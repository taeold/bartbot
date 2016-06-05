(ns bartbot.service
  (:require
    [bartbot.v1.routes :refer [v1-routes]]
    [compojure.core :refer [routes]]
    [compojure.handler :refer [api]]
    [ring.middleware.logger :as logger]))

(def handler
  (api
    (-> (routes v1-routes)
        logger/wrap-with-logger)))
