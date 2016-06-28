(ns bartbot.fb.core
  (:require
    [bartbot.config :refer [config-get]]
    [bartbot.core :as core]
    [bartbot.fb.templates :as tmpl]
    [cheshire.core :as json]
    [clj-http.client :as client]
    [clojure.tools.logging :as log]))

(def ^:private access-token (delay (config-get :api :fb :graph :page-access-token)))
(def ^:private api-host (delay (config-get :api :fb :graph :host)))

(def ^:private endpoints
  {:send-api "%s/me/messages"})

(defn send-message
  [{:as request
    :keys [recipient msg]}]
  (try
    (log/infof "sending message %s" request)
    (client/post (format (:send-api endpoints) @api-host)
                 {:content-type :json
                  :query-params {:access_token @access-token}
                  :body (json/encode {:recipient {:id recipient}
                                      :message msg})})
    (catch Exception e
      (log/infof "failed to send message %s: %s" request e))))

(defn handle-fb-chat
  [{:keys [body]
    :as request}]
  (log/infof "Processing request %s" request)
  (log/infof "Processing body %s" body)
  (let [{:as messaging
         :keys [sender message]} (-> body :entry first :messaging first)]
    (if-let [location (-> message :attachments first :payload :coordinates)]
      (if-let [recommendation (as-> location $
                                (clojure.set/rename-keys $ {:long :lon})
                                (core/get-recommendation {:location $
                                                          :maps-impl :google-maps
                                                          :transits-impl :bart}))]
        (do
          (let [elm-nearest-station (tmpl/nearest-station-template recommendation)
                elms-departures (tmpl/station-departures-template recommendation)]
            (future
              (send-message
                {:recipient (:id sender)
                 :msg {:attachment
                       {:type "template"
                        :payload {:template_type "generic"
                                  ;; performance penalty? Sure, but N < 5
                                  :elements (concat [elm-nearest-station]
                                                    elms-departures)}}}})))
          {:status 202
           :body recommendation})
        {:status 404})
      (do
        (future (send-message {:recipient (:id sender)
                               :msg {:text "sorry - i think that was invalid"}}))
        {:status 202}))))
