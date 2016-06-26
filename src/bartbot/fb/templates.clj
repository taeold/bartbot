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
               (for [departure (take 4 (:departures departures))]
                 (format "%s: %.1f minutes" (:destination departure)
                         (-> departure :departs :q (/ 60) double))))})
