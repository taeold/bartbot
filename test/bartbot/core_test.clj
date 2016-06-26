(ns bartbot.core-test
  (:require
    [clojure.test :refer :all]
    [bartbot.core :as core]
    [bartbot.maps.impl :as mimpl]
    [bartbot.maps.protocol :as mp]
    [bartbot.transits.impl :as timpl]
    [bartbot.transits.protocol :as tp]))

(deftest test-get-recommendation
  (let [maps-impl (reify mp/Maps
                    (get-nearby-places [this place location]
                      (is (= "transit-test" place))
                      (is (= {:lat 0 :lon 0} location))
                      [{:name ::station-name}])
                    (get-directions [this src dest]
                      (is (= {:lat 0 :lon 0} src))
                      (is (= {:name ::station-name} dest))
                      [::routes]))
        transits-impl (reify tp/Transit
                        (get-departures [this station opts]
                          (is (= ::station-name station))
                          ::departures))]
    (with-redefs [mimpl/maps-implementations {:maps-test (delay maps-impl)}
                  timpl/transits-implementations {:transit-test (delay transits-impl)}]
      (is (= {:route ::routes
              :departures ::departures}
             (core/get-recommendation {:location {:lat 0 :lon 0}
                                       :maps-impl :maps-test
                                       :transits-impl :transit-test}))))))

(def locations
  [;; somewhere in SF
   {:lat 37.774387 :lon -122.406966}])

(deftest ^:integration test-get-recommendation-integration
  (is (core/get-recommendation {:location (first locations)
                                :maps-impl :google-maps
                                :transits-impl :bart})))
