(ns bartbot.maps.protocol)

(defprotocol Maps
  (get-nearby-places [place location]
    "returns list of places near location")
  (get-directions [src dest]
    "returns directions from src to dest"))
