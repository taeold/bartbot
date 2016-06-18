(ns bartbot.transits.impl.bart-test
  (:require
    [clojure.test :refer :all]
    [bartbot.transits.impl.bart :as bart]
    [clj-http.client :as client]))

(deftest test-station->bart-abbrv
  (let [abbrv-station
        [["12th" "12th St. Oakland City Center"]
         ["16th" "16th St. Mission (SF)"]
         ["19th" "19th St. Oakland"]
         ["24th" "24th St. Mission (SF)"]
         ["ashb" "Ashby (Berkeley)"]
         ["balb" "Balboa Park (SF)"]
         ["bayf" "Bay Fair (San Leandro)"]
         ["cast" "Castro Valley"]
         ["civc" "Civic Center (SF)"]
         ["cols" "Coliseum"]
         ["colm" "Colma"]
         ["conc" "Concord"]
         ["daly" "Daly City"]
         ["dbrk" "Downtown Berkeley"]
         ["dubl" "Dublin/Pleasanton"]
         ["deln" "El Cerrito del Norte"]
         ["plza" "El Cerrito Plaza"]
         ["embr" "Embarcadero (SF)"]
         ["frmt" "Fremont"]
         ["ftvl" "Fruitvale (Oakland)"]
         ["glen" "Glen Park (SF)"]
         ["hayw" "Hayward"]
         ["lafy" "Lafayette"]
         ["lake" "Lake Merritt (Oakland)"]
         ["mcar" "MacArthur (Oakland)"]
         ["mlbr" "Millbrae"]
         ["mont" "Montgomery St. (SF)"]
         ["nbrk" "North Berkeley"]
         ["ncon" "North Concord/Martinez"]
         ["oakl" "Oakland Int'l Airport"]
         ["orin" "Orinda"]
         ["pitt" "Pittsburg/Bay Point"]
         ["phil" "Pleasant Hill"]
         ["powl" "Powell St. (SF)"]
         ["rich" "Richmond"]
         ["rock" "Rockridge (Oakland)"]
         ["sbrn" "San Bruno"]
         ["sfia" "San Francisco Int'l Airport"]
         ["sanl" "San Leandro"]
         ["shay" "South Hayward"]
         ["ssan" "South San Francisco"]
         ["ucty" "Union City"]
         ["wcrk" "Walnut Creek"]
         ["wdub" "West Dublin"]
         ["woak" "West Oakland"]]]
    (doseq [[abbrv station] abbrv-station]
      (testing (str station "=>" abbrv) 
        (is (= abbrv (bart/station->bart-abbrv station))))
      (testing (str station "=>" abbrv "- case insensitive") 
        (is (= abbrv (bart/station->bart-abbrv
                       (clojure.string/lower-case station))))
        (is (= abbrv (bart/station->bart-abbrv
                       (clojure.string/upper-case station))))))))

(deftest test-bart-get-real-time-departures
  (with-redefs [client/get
                (fn [url opts]
                  (is (= "http://api.bart.gov/api/etd.aspx" url))
                  (is (= {:query-params {:cmd "etd" :orig ::orig :key ::key}}
                         opts))
                  {:body
                   "<?xml version=\"1.0\" encoding=\"utf-8\"?>
                    <root>
                      <station>
                        <name>Richmond</name>
                        <abbr>RICH</abbr>
                        <etd>
                          <destination>Fremont</destination>
                          <abbreviation>FRMT</abbreviation>
                          <estimate>
                            <minutes>1</minutes>
                            <platform>2</platform>
                            <direction>South</direction>
                          </estimate>
                        </etd>
                        <etd>
                          <destination>Millbrae</destination>
                          <abbreviation>MLBR</abbreviation>
                          <estimate>
                            <minutes>2</minutes>
                            <platform>2</platform>
                            <direction>South</direction>
                          </estimate>
                        </etd>
                      </station>
                    </root>"})]
    (is (= {:station {:name "Richmond" :abbr "RICH"}
            :etd [{:destination "Fremont"
                   :estimate [{:minutes "1" :direction "South"}]}
                  {:destination "Millbrae"
                   :estimate [{:minutes "2" :direction "South"}]}]}
           (bart/bart-get-real-time-departures ::key ::orig {})))))

(deftest test-get-departures*
  (with-redefs [bart/bart-get-real-time-departures
                (fn [& _]
                  {:station {:name "Richmond" :abbr "RICH"}
                   :etd [{:destination "Fremont"
                          :estimate [{:minutes "1" :direction "South"}]}
                         {:destination "Millbrae"
                          :estimate [{:minutes "2" :direction "South"}]}]})]
    (is (= {:station "Richmond"
            :departures [{:direction "South"
                          :destination "Fremont"
                          :departs {:q 60 :u "seconds"}}
                         {:direction "South"
                          :destination "Millbrae"
                          :departs {:q 120 :u "seconds"}}]}
           (bart/get-departures* {:api-key ::key} ::station {})))))
