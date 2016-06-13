(ns bartbot.transits.protocol)

(defprotocol Transit
  (get-departures [this station opts]))
