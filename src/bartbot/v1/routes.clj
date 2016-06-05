(ns bartbot.v1.routes
  (:require [compojure.core :refer :all]))

(defroutes v1-routes
  (context "/v1" []
    (POST "/chat" request
      {:status 200
       :body (str request)})))

