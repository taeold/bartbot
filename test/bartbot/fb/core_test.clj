(ns bartbot.fb.core-test
  (:require
    [clojure.test :refer :all]
    [clj-http.client :as client]
    [bartbot.config :refer [config]]
    [bartbot.core :as core]
    [bartbot.fb.core :as fcore]
    [bartbot.fb.templates :as tmpl]))

(deftest test-send-message
  (with-redefs [config (delay {:api {:fb {:graph {:page-access-token ::token}}}})
                client/post (fn [uri req]
                              (is (= "https://graph.facebook.com/v2.6/me/messages")
                                  uri)
                              (is (= {:content-type :json
                                      :query-params {:access_token ::token}
                                      :body "{\"recipient\": {\"id\": \"id\"},\"message\":{\"text\": \"foobar\"}}"}
                                     req)))]
    (fcore/send-message {:recipient "id" :msg {:text "foobar"}})))

(deftest test-handle-fb-chat
  (testing "bad request"
    (with-redefs [fcore/send-message (constantly nil)]
      (is (= {:status 202}
             (fcore/handle-fb-chat {:body {:foo :bar}})))))
  (testing "no recommendation found"
    (with-redefs [core/get-recommendation (constantly nil)]
      (is (= {:status 404}
             (fcore/handle-fb-chat
               {:body {:entry
                       [{:messaging
                         [{:message
                           {:attachments
                            [{:payload
                              {:coordinates
                               {:lat ::lat :lon ::lon}}}]}}]}]}})))))
  (testing "returns recommendation given proper body"
    (with-redefs [tmpl/nearest-station-template (constantly nil)
                  tmpl/station-departures-template (constantly nil)
                  fcore/send-message
                  ;; TODO: this should test what msg is being send to fb
                  (constantly nil)
                  core/get-recommendation
                  (fn [req]
                    (is (= {:lat ::lat :lon ::lon} (:location req)))
                    (is (= :google-maps (:maps-impl req)))
                    (is (= :bart (:transits-impl req)))
                    {:route ::route
                     :departures ::departures})]
      (let [ex-fb-request
            {:object "page"
             :entry
             [{:id "1634911133499510"
               :time 1466930657974
               :messaging
               [{:id "10208562629875976"
                 :sender {:id "10208562629875976"}
                 :recipient {:id "1634911133499510"}
                 :timestamp 1466930657932
                 :message
                 {:mid "mid.1466930657780:a6d5e6bd2feac2aa98"
                  :seq 23
                  :attachments [{:title "Daniel's Location"
                                 :url "REDACTED"
                                 :type "location"
                                 :payload {:coordinates
                                           {:lat ::lat :long ::lon}}}]}}]}]}]
        (is (= {:status 202 :body {:route ::route :departures ::departures}}
               (fcore/handle-fb-chat {:body ex-fb-request})))))))
