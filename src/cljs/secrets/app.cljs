 ;;Requirements
 ;;1. Submit a Secret, get confirmation back - [x]
 ;;2. Submit a Code, get Response - [ ]
 ;; - If approved, get success response and download - [ ] 200
 ;; - If denied,   get denied response - [ ] (402 payment required)
 ;; - If pending,  get pending response - [ ] (204 no content)
 ;;3. Encrypt Secrets in database - [ ]
 ;;4. Routes?
 ;;5. Styling - [ ]

(ns secrets.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))
(enable-console-print!)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STATE
(def state (r/atom {:secret ""
                    :code ""
                    :prompt "they sends the code, we sends the response"}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; HTTP REQUESTS AND HANDLERS
(defn post-secret-handler [response] (swap! state #(assoc % :prompt (concat "Your code " (:code response)))))
(defn post-secret []
  (POST "/secrets"
    {:format :json
     :keywords? true
     :response-format :json
     :params {:text (:secret @state)}
     :handler post-secret-handler}))

(defn update-prompt [new-prompt] (swap! state #(assoc % :prompt new-prompt)))
(defn check-secret-status-handler [response]
  (cond
    (= (:status response) "PENDING") (update-prompt "their secrets has not been weighed yet")
    (= (:status response) "APPROVED") (update-prompt "their secrets has been found worthy!")))

(defn check-secret-status-error-handler [response]
  (let [status (:status response)]
    (cond
      (= status 402) (update-prompt "their secrets was weighed and found wanting")
      :else (println "code not found"))))

(defn check-secret-status []
  (let [url (str "secrets/" (:code @state))]
    (GET url
      {:format :json
       :response-format :json
       :keywords? true
       :handler check-secret-status-handler
       :error-handler check-secret-status-error-handler})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; APP STATE HANDLERS
(defn update-secret [event]
  (swap! state #(assoc % :secret (-> event .-target .-value))))
(defn update-code [event]
  (swap! state #(assoc % :code (-> event .-target .-value))))

(defn app []
  [:div
    [:h2 "gives a secrets, gets a musics"]
    [:p "they writes the secret, they clicks the send, we reviews the secret, we send the code, they writes the code somewhere safe"]
    [:p "we waits, we reads the secret, we rates the secret"]
    [:p "they comes back, they writes the code, they clicks the send, if the secret is tasty, we sends the music"]
    [:hr]
    [:div
      [:textarea {:rows 30 :placeholder "enter a secret" :value (:secret @state) :on-change update-secret}]
      [:button {:on-click post-secret} "Submit Secret"]]
    [:hr]
    [:div
      [:p (:prompt @state)]
      [:input {:placeholder "enter a code" :value (:code @state) :on-change update-code}]
      [:button {:on-click check-secret-status}  "Check status by entering code"]]
    [:div
      [:button {:on-click get-music} "You're Approved! Click Here To Download!"]]])

(defn render-app []
    (r/render [app]
      (.getElementById js/document "root")))

(render-app)
