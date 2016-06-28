(ns bartbot.fb.templates
  "barrbot message templates"
  (:require
    [bartbot.fb.schema :as fb-schema]
    [bartbot.schema :as bb-schema]
    [schema.core :as schema]))

(schema/defn nearest-station-template :- fb-schema/FBSendMessage
  [{:as recommend
    :keys [departures route]} :- bb-schema/BartbotRecommends]
  {:title "Station Near You"
   :item_url (:url route)
   :subtitle (format "%s is %.1f minutes away (%.1f miles)"

                     (:station departures)
                     (-> route :duration :q (/ 60) double)
                     (-> route :distance :q (* 0.000621371) double))})

(schema/defn station-departures-template :- [fb-schema/FBSendMessage]
  [{:as recommend
    :keys [departures]} :- bb-schema/BartbotRecommends]
  (let [station (:station departures)
        departures-pair (->> departures
                             :departures
                             (group-by :destination)
                             ;; each message contains info for at most 2 dest
                             (partition 2 2 nil))]
    (for [[i departures] (map-indexed vector departures-pair)]
      {:title (if (= 0 i)
                (format "Departures at %s" station)
                (format "Departures at %s (cont)" station))
       :subtitle (clojure.string/join "\n"
                   (for [[dest departure] departures
                         :let [depart-times (->> departure
                                                 (sort-by (comp :q :departs))
                                                 (map #(-> %
                                                           :departs
                                                           :q
                                                           (/ 60)
                                                           double)))
                               departs (->> depart-times
                                            (map #(format "%.1f" %))
                                            (clojure.string/join ", "))]]
                                       (format "%s: %s minutes" dest departs)))})))
