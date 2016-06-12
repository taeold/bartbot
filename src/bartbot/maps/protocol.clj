(ns bartbot.maps.protocol)

(defprotocol Maps
  (get-nearby-places [this place location]
    "returns list of places nearby location")
  (get-directions [this src dest]
    "returns directions from src to dest"))
