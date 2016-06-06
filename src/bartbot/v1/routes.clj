(ns bartbot.v1.routes
  (:require
    [bartbot.config :refer [config]]
    [compojure.core :refer :all]
    [immuconf.config :as immuconf]))

(defroutes v1-routes
  (context "/v1" []
    (GET "/chat" {{challenge "hub.challenge"
                   verify-token "hub.verify_token"} :params}
      (if (= (immuconf/get @config :fb :verify-token) verify-token)
        {:status 200 :body challenge}
        {:status 401}))
    (POST "/chat" request
      {:status 200
       :body (str request)})))

