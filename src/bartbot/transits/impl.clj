(ns bartbot.transits.impl
  (:require
    [bartbot.config :refer [config-get]]
    [bartbot.transits.impl.bart :refer [->Bart]]
    [bartbot.transits.protocol :as tproto]))

(def transits-implementations
  {:bart (delay
           (->Bart {:api-key (config-get :api :bart :api-key)}))})
