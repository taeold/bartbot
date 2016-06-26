(ns bartbot.fb.templates
  "barrbot message templates"
  (:require
    [bartbot.schema :as bb-schema]
    [schema.core :as schema]))

(schema/defn nearest-station-template
  [{:as recommend
    :keys [departures route]} :- bb-schema/BartbotRecommends]
  {:title "Nearest Station"
   :item_url (:url route)
   :subtitle (format "Nearest station %s is %.1f minutes away (%.1f miles)"

                     (:station departures)
                     (-> route :duration :q (/ 60) double)
                     (-> route :distance :q (* 0.000621371) double))})

(schema/defn station-departures-template
  [{:as recommend
    :keys [departures]} :- bb-schema/BartbotRecommends]
  {:title (format "Departures at %s" (:station departures))
   :subtitle (clojure.string/join "\n"
               (for [[dest departures] (group-by :destination
                                                 (:departures departures))
                     :let [depart-times (->> departures
                                             (sort-by (comp :q :departs))
                                             (map #(-> %
                                                       :departs
                                                       :q
                                                       (/ 60)
                                                       double)))
                           departs (->> depart-times
                                        (map #(format "%.1f" %))
                                        (clojure.string/join ", "))]]
                 (format "%s: %s minutes" dest departs)))})
