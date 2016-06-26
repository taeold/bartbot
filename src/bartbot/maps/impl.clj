(ns bartbot.maps.impl
  (:require
    [bartbot.config :refer [config-get]]
    [bartbot.maps.impl.google :refer [->GoogleMaps]]))

(def maps-implementations
  {:google-maps (delay
                  (->GoogleMaps
                    {:api-key (config-get :api :google :maps :api-key)}))})
