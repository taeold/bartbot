(ns bartbot.config
  (:require
    [immuconf.config :as immuconf]))

(def config
  (delay (immuconf/load "resources/config.edn" "resources/private/config.edn")))
