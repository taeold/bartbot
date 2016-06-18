(ns bartbot.maps.impl.google-test
  (:require
    [clojure.test :refer :all]
    [bartbot.config :refer [config-get]]
    [bartbot.maps.impl.google :as google]
    [clj-http.client :as client]
    [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(deftest test-google-get-nearby-places
  (testing "Formulates request correctly"
    (with-redefs [client/get
                  (fn [url opts]
                    (is (= "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                           url))
                    (is (= {:as :json
                            :query-params {:rankby "distance"
                                           :keyword ::search-key
                                           :location ::location
                                           :key ::api-key}}
                           opts))
                    {:body {:status "OK"
                            :results [::foobar]}})]
      (google/google-get-nearby-places ::api-key ::search-key ::location)))
    (testing "Handles response status accordingly"
      (with-redefs [client/get (fn [& _]
                                 {:body {:status "OK"
                                         :results [::foobar]}})]
        (is (= [::foobar]
               (google/google-get-nearby-places ::api-key ::search-key ::location))))
      (with-redefs [client/get (fn [& _]
                                 {:body {:status "ZERO_RESULTS"
                                         :results [::foobar]}})]
        (is (= []
               (google/google-get-nearby-places ::api-key ::search-key ::location))))
      (with-redefs [client/get (fn [& _]
                                 {:body {:status "SOME_ERROR"
                                         :error_message "foobar"}})]
        (is (thrown? Exception
                     (google/google-get-nearby-places ::api-key ::search-key ::location))))))

(deftest test-google-get-directions
  (testing "Formulates request correctly"
    (with-redefs [client/get
                  (fn [url opts]
                    (is (= "https://maps.googleapis.com/maps/api/directions/json"
                           url))
                    (is (= {:as :json
                            :query-params {:mode "walking"
                                           :destination ::destination
                                           :origin ::origin
                                           :key ::api-key}}
                           opts))
                    {:body {:status "OK"
                            :routes [::foobar]}})]
      (google/google-get-directions ::api-key ::origin ::destination)))
  (testing "Handles response status accordingly"
    (with-redefs [client/get (fn [& _]
                               {:body {:status "OK"
                                       :routes [::foobar]}})]
      (is (= [::foobar]
             (google/google-get-directions ::api-key ::origin ::destination))))
    (with-redefs [client/get (fn [& _]
                               {:body {:status "ZERO_RESULTS"
                                       :routes [::foobar]}})]
      (is (= []
             (google/google-get-directions ::api-key ::origin ::destination))))
    (with-redefs [client/get (fn [& _]
                               {:body {:status "SOME_ERROR"
                                       :error_message "foobar"}})]
      (is (thrown? Exception
                   (google/google-get-directions ::api-key ::origin ::destination))))))

(deftest test-get-nearby-places*
  (with-redefs [google/google-get-nearby-places
                (fn [api-key search-key location]
                  (is (= ::api-key api-key))
                  (is (= "search" search-key))
                  (is (= "123,123" location))
                  [{:place_id "place-id"
                    :name "name"
                    :geometry {:location {:lng 123 :lat 123}}}])]
    (is (= [{:source "google"
             :id "place-id"
             :name "name"
             :location {:lat 123 :lon 123}}]
           (google/get-nearby-places*
             {:api-key ::api-key} "search" {:lat 123 :lon 123})))))

(deftest test-get-directions*
  (with-redefs [google/google-get-directions
                (fn [api-key src dest]
                  (is (= ::api-key api-key))
                  (is (= "123,123" src))
                  (is (= "place_id:place" dest))
                  [{:legs
                    [{:distance {:text "X mi" :value 1}
                      :duration {:text "X mins" :value 1}}]}])]
    (is (= [{:source "google"
             :distance {:q 1 :u "meters"}
             :duration {:q 1 :u "seconds"}}]
           (google/get-directions* {:api-key ::api-key}
                                   {:lat 123 :lon 123}
                                   {:source "test-source"
                                    :name "test-location"
                                    :location {:lat 1 :lon 2}
                                    :id "place"})))))

(def api-key
  (config-get :google :maps :api-key))
(def locations
  [;; somewhere in SF
   {:lat 37.774387 :lon -122.406966}])
(def places
  [{:source "google"
    :id "ChIJYTKuRpuAhYAR8O67wA_IE9s"
    :name "Civic Center/UN Plaza Station"
    :location {:lat 37.7844688, :lon -122.4079864}}])

(deftest ^:integration test-get-nearby-places*-integration
  (is (google/get-nearby-places*
        {:api-key api-key} "bart station" (first locations))))

(deftest ^:integration test-get-directions*-integration
  (is (google/get-directions*
        {:api-key api-key} (first locations) (first places))))
