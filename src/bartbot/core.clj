(ns bartbot.core
  (:require
    [bartbot.maps.impl :refer [maps-implementations]]
    [bartbot.maps.protocol :as mp]
    [bartbot.schema :as bb-schema]
    [bartbot.transits.impl :refer [transits-implementations]]
    [bartbot.transits.protocol :as tp]
    [schema.core :as schema]))

(schema/defn get-recommendation :- (schema/maybe bb-schema/BartbotRecommends)
  [{:as request
    location :location
    maps-impl-k :maps-impl
    transits-impl-k :transits-impl} :- bb-schema/BartbotRequest]
  (let [maps-impl @(maps-implementations maps-impl-k)
        transits-impl @(transits-implementations transits-impl-k)
        nearest-station (-> (mp/get-nearby-places
                              maps-impl (name transits-impl-k) location)
                            first)
        route (first (mp/get-directions maps-impl location nearest-station))
        depatures (tp/get-departures transits-impl (:name nearest-station) {})]
    (if (and route depatures)
      {:route route
       :departures depatures})))
