(ns bartbot.maps.protocol)

(defprotocol Maps
  (get-nearby-places [place location]
    "returns list of places near location")
  (get-direction [src dest]
    "returns direction from src to dest"))
