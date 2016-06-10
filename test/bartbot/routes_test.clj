(ns bartbot.routes-test
  (:require
    [clojure.test :refer :all]
    [bartbot.config :refer [config]]
    [bartbot.routes :as routes]))

(deftest test-service-routes
  (testing "GET /fb/chat"
    (with-redefs [config (delay {:fb {:verify-token :token}})]
      (testing "fb api token verification succeeds with correct token"
        (is (= {:status 200
                :headers {}
                :body "challenge"}
               (routes/service-routes
                 {:request-method :get
                  :uri "/fb/chat"
                  :params {"hub.challenge" "challenge"
                           "hub.verify_token" :token}}))))
      (testing "fb api token verification fails with wrong token"
        (is (= {:status 401
                :headers {}
                :body ""}
               (routes/service-routes
                 {:request-method :get
                  :uri "/fb/chat"
                  :params {"hub.challenge" "challenge"
                           "hub.verify_token" :wrong}}))))))
  (testing "POST /fb/chat"
    (with-redefs [config (delay {:fb {:verify-token :token}})]
      (testing "request is denied with wrong token"
        (is (= {:status 401
                :headers {}
                :body ""}
               (routes/service-routes
                 {:request-method :post
                  :uri "/fb/chat"
                  :params {"hub.verify_token" :wrong}})))))))
