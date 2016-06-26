(ns bartbot.fb.routes-test
  (:require
    [clojure.test :refer :all]
    [bartbot.config :refer [config]]
    [bartbot.fb.routes :as routes]))

(deftest test-fb-routes
  (testing "GET /fb/chat"
    (with-redefs [config (delay {:api {:fb {:messenger {:verify-token ::token}}}})]
      (testing "fb api token verification succeeds with correct token"
        (is (= {:status 200
                :headers {}
                :body "challenge"}
               (routes/fb-routes
                 {:request-method :get
                  :uri "/fb/chat"
                  :params {"hub.challenge" "challenge"
                           "hub.verify_token" ::token}}))))
      (testing "fb api token verification fails with wrong token"
        (is (= {:status 401
                :headers {}
                :body ""}
               (routes/fb-routes
                 {:request-method :get
                  :uri "/fb/chat"
                  :params {"hub.challenge" "challenge"
                           "hub.verify_token" ::wrong}})))))))
