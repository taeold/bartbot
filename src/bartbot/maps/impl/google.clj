(ns bartbot.maps.impl.google
  (:require
    [bartbot.config :refer [config-get]]
    [bartbot.maps.protocol :as protocol]
    [bartbot.maps.schema :as map-schema]
    [clj-http.client :as client]
    [schema.core :as schema]))

(def ^:private api-host (delay (config-get :api :google :maps :host)))

(def ^:private endpoints
  {:nearby "%s/place/nearbysearch/json"
   :directions "%s/directions/json"})

(defn google-get-nearby-places
  "Ask google maps about places described via search-key near given location.
  Location should be a string of form '{lat},{lon}'.

  API doc: https://developers.google.com/places/web-service/search"
  [api-key search-key location]
  (let [body (-> (client/get
                   (format (:nearby endpoints) @api-host)
                   {:as :json
                    :query-params {:rankby "distance"
                                   :keyword search-key
                                   :location location
                                   :key api-key}})
                 :body)]
    (case (:status body)
      "OK" (:results body)
      "ZERO_RESULTS" []
      (throw (Exception. (str (:status body) ":" (:error_message body)))))))

(defn google-get-directions
  "Ask google maps about *walking* direction from src to dest. src and dest
  can be search keyword, '{lat},{lon}', or 'place_id:{place_id}'.

  API doc: https://developers.google.com/maps/documentation/directions/start"
  [api-key src dest]
  (let [body (-> (client/get
                   (format (:directions endpoints) @api-host)
                   {:as :json
                    :query-params {:mode "walking"
                                   :destination dest
                                   :origin src
                                   :key api-key}})
                 :body)]
    (case (:status body)
      "OK" (:routes body)
      "ZERO_RESULTS" []
      (throw (Exception. (str (:status body) ":" (:error_message body)))))))

(schema/defn get-nearby-places* :- [map-schema/Place]
  [{:as opts :keys [api-key]}
   search-key :- schema/Str
   location :- map-schema/Location]
  (let [location-str (str (:lat location) "," (:lon location))
        places (google-get-nearby-places api-key search-key location-str)]
    (for [place places]
      {:source "google"
       :id (:place_id place)
       :name (:name place)
       :location (-> (get-in place [:geometry :location])
                     (clojure.set/rename-keys {:lng :lon}))})))

(schema/defn get-directions* :- [map-schema/Route]
  [{:as opts :keys [api-key]}
   src :- map-schema/Location
   dest :- map-schema/Place]
  (let [src-str (str (:lat src) "," (:lon src))
        dest-str (str "place_id:" (:id dest))
        routes (google-get-directions api-key src-str dest-str)]
    (for [route routes]
      {:source "google"
       ;; since no waypoint is specified, we assume single leg
       :distance {:q (get-in route [:legs 0 :distance :value])
                  :u "meters"}
       :duration {:q (get-in route [:legs 0 :duration :value])
                  :u "seconds"}})))

(deftype GoogleMaps [client-opts]
  protocol/Maps
  (get-nearby-places [this place location]
    (get-nearby-places* client-opts place location))
  (get-directions [this src dest]
    (get-directions* client-opts src dest)))
