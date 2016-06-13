(ns bartbot.transits.impl.bart
  (:require
    [bartbot.config :refer [config-get]]
    [bartbot.transits.protocol :as protocol]
    [bartbot.transits.schema :as transits-schema]
    [clj-http.client :as client]
    [clojure.data.xml :as xml]
    [clojure.data.zip :as zf]
    [clojure.data.zip.xml :refer [xml-> xml1-> text]]
    [clojure.zip :as zip]
    [schema.core :as schema]))

(def ^:private api-host (delay (config-get :bart :host)))

(def ^:private endpoints
  {:etd "%s/etd.aspx"})

(defn bart-get-real-time-departures
  "Ask bart about real time departure status for a station.

  Station must be one of the abbreviated station names (e.g. Powell -> powl).
  You can also provide optional parameters: platform must be an integer, and
  direction must be either 'n' or 's' (to indiciate north or south).

  BART Api returns responses in XML (gasp) - we try to transform it into
  a map using zipper.

  API docs: http://api.bart.gov/docs/etd/etd.aspx"
  [api-key orig {:as opts :keys [plat dir]}]
  (let [body (-> (client/get
                   (format (:etd endpoints) @api-host)
                   {:query-params (merge {:cmd "etd"
                                          :orig orig
                                          :key api-key}
                                         (when plat
                                           {:plat plat})
                                         (when dir
                                           {:dir dir}))})
                 :body
                 xml/parse-str
                 zip/xml-zip)
        etds (xml-> body :station :etd)]
    ;; let's transform the xml response into a map
    {:station {:name (xml1-> body :station :name text)
               :abbr (xml1-> body :station :abbr text)}
     :etd (for [etd etds]
            {:destination (xml1-> etd :destination text)
             :estimate (for [estimate (xml-> etd :estimate)]
                         {:minutes (xml1-> estimate :minutes text)
                          :direction (xml1-> estimate :direction text)})})}))

(schema/defn get-departures* :- transits-schema/Departures
  [{:keys [api-key] :as client-opts}
   station :- schema/Str
   opts :- {(schema/optional-key :platform) schema/Str
            (schema/optional-key :direction) schema/Str}]
  ;; TODO: standardize station name
  (let [bart-departures (bart-get-real-time-departures api-key station opts)
        ;; we reformat the data to fit transits-schema/Departure
        departures (->> (for [etd (:etd bart-departures)]
                          (for [estimate (:estimate etd)]
                            {:destination (:destination etd)
                             :direction (:direction estimate)
                             :departs {:q (try
                                            (-> estimate
                                                :minutes
                                                Integer/parseInt
                                                (* 60))
                                            (catch NumberFormatException _
                                              ;; estimates could non-number
                                              ;; e.g. 'LEAVING'
                                              0))
                                       :u "seconds"}}))
                        (apply concat))]
    {:station (get-in bart-departures [:station :name])
     :departures (sort-by #(get-in % [:departs :q]) departures)}))

(deftype Bart [client-opts]
  protocol/Transit
  (get-departures [this station opts]
    (get-departures* client-opts station opts)))
