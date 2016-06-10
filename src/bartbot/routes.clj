(ns bartbot.routes
  (:require
    [bartbot.config :refer [config]]
    [compojure.core :refer :all]
    [immuconf.config :as immuconf]))

(defroutes service-routes
  (context "/fb" []
    (GET "/chat" {{challenge "hub.challenge"
                   verify-token "hub.verify_token"} :params}
      (if (= (immuconf/get @config :fb :verify-token) verify-token)
        {:status 200 :body challenge}
        {:status 401}))
    (POST "/chat" {:as request
                   {verify-token "hub.verify_token"} :params}
      (if (= (immuconf/get @config :fb :verify-token) verify-token)
        {:status 200
         :body (str request)}
        {:status 401}))))