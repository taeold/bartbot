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
  (let [{:as messaging
         :keys [sender message]} (-> body :entry first :messaging first)]
    (if-let [location (-> message :attachments first :payload :coordinates)]
      (if-let [recommendation (as-> location $
                                (clojure.set/rename-keys $ {:long :lon})
                                (core/get-recommendation {:location $
                                                          :maps-impl :google-maps
                                                          :transits-impl :bart}))]
        (do
          (future
            (send-message
              {:recipient (:id sender)
               :msg {:attachment
                     {:type "template"
                      :payload {:template_type "generic"
                                :elements [(tmpl/nearest-station-template
                                             recommendation)
                                           (tmpl/station-departures-template
                                             recommendation)]}}}}))
          {:status 200
           :body recommendation})
        {:status 404})
      {:status 400})))
