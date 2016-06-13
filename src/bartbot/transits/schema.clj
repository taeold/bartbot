(ns bartbot.transits.schema
  (:require
    [schema.core :as schema]))

(def Departure
  {:direction schema/Str
   :destination schema/Str
   :departs {:q schema/Num
             :u (schema/enum "seconds")}})

(def Departures
  {:station schema/Str
   :departures [Departure]})
