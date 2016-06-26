(ns bartbot.fb.templates-test
  (:require
    [clojure.test :refer :all]
    [bartbot.fb.templates :as templ]))

(deftest test-nearest-station-template
  (is (= {:title "Nearest Station"
          :item_url "http://foobar.com"
          :subtitle "Nearest station x is 1.5 minutes away (0.1 miles)"}
        (templ/nearest-station-template {:route {:url "http://foobar.com"
                                                 :distance {:q 100
                                                            :u "meters"}
                                                 :duration {:q 90
                                                            :u "seconds"}}
                                         :departures {:station "x"}}))))

(deftest test-station-departures-template
  (is (= {:title "Departures at x"
          :subtitle "a: 1.5 minutes\nb: 1.5, 1.8 minutes"}
         (templ/station-departures-template
           {:departures {:station "x"
                         :departures [{:direction "North"
                                       :destination "a"
                                       :departs {:q 90 :u "seconds"}}
                                      {:direction "North"
                                       :destination "b"
                                       :departs {:q 110 :u "seconds"}}
                                      {:direction "North"
                                       :destination "b"
                                       :departs {:q 90 :u "seconds"}}]}}))))
