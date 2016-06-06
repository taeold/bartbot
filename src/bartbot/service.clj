(ns bartbot.service
  (:require
    [bartbot.v1.routes :refer [v1-routes]]
    [compojure.core :refer [routes]]
    [compojure.handler :refer [api]]
    [compojure.route :refer [not-found]]
    [ring.middleware.logger :as logger]))

(def handler
  (api
    (-> (routes
          v1-routes
          (not-found "<h1>Page not found</h1>"))
        logger/wrap-with-logger)))
