(ns bartbot.service
  (:require
    [bartbot.routes :refer [service-routes]]
    [compojure.core :refer [routes]]
    [compojure.handler :refer [api]]
    [compojure.route :refer [not-found]]
    [ring.logger :as logger]))

(def handler
  (api
    (-> (routes
          service-routes
          (not-found "<h1>Page not found</h1>"))
        (logger/wrap-with-logger {:printer :no-color}))))
