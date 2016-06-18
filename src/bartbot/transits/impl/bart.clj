(ns bartbot.transits.impl.bart
  (:require
    [bartbot.config :refer [config-get]]
    [bartbot.transits.protocol :as protocol]
    [bartbot.transits.schema :as transits-schema]
    [clj-http.client :as client]
    [clojure.data.xml :as xml]
    [clojure.data.zip.xml :refer [xml-> xml1-> text]]
    [clojure.zip :as zip]
    [schema.core :as schema]))

(def ^:private api-host (delay (config-get :bart :host)))

(def ^:private endpoints
  {:etd "%s/etd.aspx"})

(def ^:private station-regex
  ;; maps regex pattern to abbreviated station name
  ;; http://api.bart.gov/docs/overview/abbrev.aspx
  [{:regex #"(?i)12th"                  :abbrv "12th"} ;; 12th St. Oakland City Center
   {:regex #"(?i)16th"                  :abbrv "16th"} ;; 16th St. Mission (SF)
   {:regex #"(?i)19th"                  :abbrv "19th"} ;; 19th St. Oakland
   {:regex #"(?i)24th"                  :abbrv "24th"} ;; 24th st. Missoin (SF)
   {:regex #"(?i)ashby"                 :abbrv "ashb"} ;; Ashby (Berkeley)
   {:regex #"(?i)balboa park"           :abbrv "balb"} ;; Balboar Park (SF)
   {:regex #"(?i)bay fair"              :abbrv "bayf"} ;; Bay Fair (San Leandro)
   {:regex #"(?i)castro valley"         :abbrv "cast"} ;; Castro Valley
   {:regex #"(?i)civic center"          :abbrv "civc"} ;; Civic Center (SF)
   {:regex #"(?i)coliseum"              :abbrv "cols"} ;; Coliseum
   {:regex #"(?i)colma"                 :abbrv "colm"} ;; Colma
   {:regex #"(?i)^concord"              :abbrv "conc"} ;; Concord
   {:regex #"(?i)daly city"             :abbrv "daly"} ;; Daly City
   {:regex #"(?i)downtown berkeley"     :abbrv "dbrk"} ;; Downtown Berkeley
   {:regex #"(?i)pleasanton"            :abbrv "dubl"} ;; Dublin/Pleasanton
   {:regex #"(?i)el cerrito del norte"  :abbrv "deln"} ;; El Cerrito del Norte
   {:regex #"(?i)el cerrito plaza"      :abbrv "plza"} ;; El cerrito Plaza
   {:regex #"(?i)embarcadero"           :abbrv "embr"} ;; Embarcadero (SF)
   {:regex #"(?i)fremont"               :abbrv "frmt"} ;; Fremont
   {:regex #"(?i)fruitvale"             :abbrv "ftvl"} ;; Fruitvale
   {:regex #"(?i)glen park"             :abbrv "glen"} ;; Glen Park (SF)
   {:regex #"(?i)^hayward"              :abbrv "hayw"} ;; Hayward
   {:regex #"(?i)lafayette"             :abbrv "lafy"} ;; Lafayette
   {:regex #"(?i)lake merritt"          :abbrv "lake"} ;; Lake Merritt (Oakland)
   {:regex #"(?i)macarthur"             :abbrv "mcar"} ;; MacArthur (Oakland)
   {:regex #"(?i)millbrae"              :abbrv "mlbr"} ;; Millbrae
   {:regex #"(?i)montgomery"            :abbrv "mont"} ;; Montgomery st. (SF)
   {:regex #"(?i)north berkeley"        :abbrv "nbrk"} ;; North Berkeley
   {:regex #"(?i)north concord"         :abbrv "ncon"} ;; North Concord/Martinez
   {:regex #"(?i)oakland.*airport"      :abbrv "oakl"} ;; Oakland Int'l Airport
   {:regex #"(?i)orinda"                :abbrv "orin"} ;; Orinda
   {:regex #"(?i)pittsburg"             :abbrv "pitt"} ;; Pittsburg/Bay Point
   {:regex #"(?i)pleasant hill"         :abbrv "phil"} ;; Pleasant Hill
   {:regex #"(?i)powell"                :abbrv "powl"} ;; Powell St. (SF)
   {:regex #"(?i)richmond"              :abbrv "rich"} ;; Richmond
   {:regex #"(?i)rockridge"             :abbrv "rock"} ;; Rockridge (Oakland)
   {:regex #"(?i)san bruno"             :abbrv "sbrn"} ;; San Bruno
   {:regex #"(?i)san francisco.*airport" :abbrv "sfia"} ;; San Francisco Int'l Airport
   {:regex #"(?i)san leandro"           :abbrv "sanl"} ;; San Leandro
   {:regex #"(?i)south hayward"         :abbrv "shay"} ;; South Hayward
   {:regex #"(?i)south san francisco"   :abbrv "ssan"} ;; South San Francisco
   {:regex #"(?i)union city"            :abbrv "ucty"} ;; Union City
   {:regex #"(?i)walnut creek"          :abbrv "wcrk"} ;; Walnut Creek
   {:regex #"(?i)west dublin"           :abbrv "wdub"} ;; West Dublin
   {:regex #"(?i)west oakland"          :abbrv "woak"} ;; West Oakland
   ])

(defn station->bart-abbrv
  "Converts station name to abbreviated station name"
  [station]
  (:abbrv
    (some #(if (re-find (:regex %) station) %) station-regex)))

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
