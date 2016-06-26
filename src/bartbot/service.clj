(ns bartbot.service
  (:require
    [bartbot.routes :refer [service-routes]]
    [compojure.core :refer [routes]]
    [compojure.handler :refer [api]]
    [compojure.route :refer [not-found]]
    [ring.logger :as logger]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def handler
  (api
    (-> (routes
          service-routes
          (not-found "<h1>Page not found</h1>"))
        (logger/wrap-with-logger {:printer :no-color})
        wrap-json-body
        wrap-json-response)))
