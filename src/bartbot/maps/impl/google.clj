(ns bartbot.maps.impl.google
  (:require
    [bartbot.config :refer [config]]
    [bartbot.maps.protocol :as protocol]
    [clj-http.client :as client]
    [immuconf.config :as immuconf]
    [schema.core :as schema]))

(def ^:private api-host (immuconf/get @config :google :maps :host))

(schema/defn get-nearby-places*
  ;; TODO:
  ;; setup schema for return types
  ;; setup schema for inputs
  [opts place location]
  nil)

(schema/defn get-direction*
  [opts src dest]
  )

(deftype GoogleMaps [client-opts]
  protocol/Maps
  (get-nearby-places [place location]
    (get-nearby-places* client-opts place location))
  (get-direction [src dest]
    (get-direction* client-opts src dest)))
