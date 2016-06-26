(ns bartbot.fb.routes
  (:require
    [bartbot.config :refer [config-get]]
    [bartbot.fb.core :refer [handle-fb-chat]]
    [compojure.core :refer :all]))

(defroutes fb-routes
  (context "/fb" []
    (GET "/chat" {{challenge "hub.challenge"
                   verify-token "hub.verify_token"} :params}
      (if (= (config-get :api :fb :messenger :verify-token) verify-token)
        {:status 200 :body challenge}
        {:status 401}))
    (POST "/chat" request
      (handle-fb-chat request))))
