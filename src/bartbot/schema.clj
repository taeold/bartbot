(ns bartbot.schema
  (:require
    [bartbot.maps.impl :refer [maps-implementations]]
    [bartbot.maps.schema :as m-schema]
    [bartbot.transits.impl :refer [transits-implementations]]
    [bartbot.transits.schema :as t-schema]
    [schema.core :as schema]))

(def BartbotRequest
  {:location m-schema/Location
   :maps-impl (schema/enum (keys maps-implementations))
   :transits-impl (schema/enum (keys transits-implementations))})

(def BartbotRecommends
  {:route m-schema/Route
   :departures t-schema/Departures})
