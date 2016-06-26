(ns bartbot.maps.schema
  (:require
    [schema.core :as schema]))

(def Location
  {:lat schema/Num
   :lon schema/Num})

(def Place
  {:source schema/Str
   :id schema/Str
   :name schema/Str
   :location Location})

(def Route
  {:source schema/Str
   :url schema/Str
   :distance {:q schema/Num
              :u (schema/enum "meters")}
   :duration {:q schema/Num
              :u (schema/enum "seconds")}})
