(ns bartbot.fb.schema
  (:require
    [schema.core :as schema]))

(def FBSendMessage
  {:title schema/Str
   :subtitle schema/Str
   (schema/optional-key :item_url) schema/Str
   (schema/optional-key :image_url) schema/Str})
