(ns bartbot.config
  (:require
    [immuconf.config :as immuconf]))

(def config
  (delay (immuconf/load "resources/config.edn" "resources/private/config.edn")))

(defn config-get
  [& keys]
  (apply (partial immuconf/get @config) keys))
