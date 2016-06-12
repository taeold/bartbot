(ns bartbot.routes
  (:require
    [bartbot.config :refer [config-get]]
    [compojure.core :refer :all]))

(defroutes service-routes
  (context "/fb" []
    (GET "/chat" {{challenge "hub.challenge"
                   verify-token "hub.verify_token"} :params}
      (if (= (config-get :fb :messenger :verify-token) verify-token)
        {:status 200 :body challenge}
        {:status 401}))
    (POST "/chat" {:as request
                   {verify-token "hub.verify_token"} :params}
      (if (= (config-get :fb :messenger :verify-token) verify-token)
        {:status 200
         :body (str request)}
        {:status 401}))))
