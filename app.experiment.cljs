 ;;Requirements
 ;;1. Submit a Secret, get confirmation back - [x]
 ;;2. Submit a Code, get Response - [ ]
 ;; - If approved, get success response and download - [ ] 200
 ;; - If denied,   get denied response - [ ] (402 payment required)
 ;; - If pending,  get pending response - [ ] (204 no content)
 ;;3. Encrypt Secrets in database - [ ]
 ;;4. Routes?
 ;;5. Styling - [ ]

(ns compojore-secrets.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))
(enable-console-print!)

(def state (r/atom {:secrets [{:text "secrets haven't been fetched yet"}]
                    :secret ""
                    :code ""
                    :prompt "Send me a secret and if it is deemed worthy you will be rewarded with the luscious sounds of Agatha Frisky"}))

(defn list-secrets []
  [:ul
    (for [secret (:secrets @state)]
      ^{:key secret} [:li (:text secret)])])

(defn post-secret-handler [response] (swap! state #(assoc % :prompt (concat "Your code " (:code response)))))
(defn get-music-handler [response] (println (:headers response)))

(defn get-secrets-handler [response]
  (swap! state #(assoc % :secrets response)))


(defn get-secrets []
  (GET "/secrets"
    {:response-format :json
      :keywords? true
      :handler get-secrets-handler}))

(defn post-secret []
  (POST "/secrets"
    {:format :json
     :keywords? true
     :response-format :json
     :params {:text (:secret @state)}
     :handler post-secret-handler}))

(defn clear-secrets []
  (swap! state (fn [a] (assoc a :secrets []))))

(defn update-secret [event]
  (swap! state (fn [a] (assoc a :secret (-> event .-target .-value)))))

(defn update-code [event]
  (swap! state (fn [a] (assoc a :code (-> event .-target .-value)))))

(defn get-music []
  (let [url (str "secrets/" (:code @state))]
    (GET url
      {:format :json
       :response-format :json
       :keywords? true
       :handler get-music-handler})))

(defn app []
  [:div
    [:h2 "secrets"]
    [:p (:prompt @state)]
    [:button {:on-click get-secrets} "Get Secrets"]
    [:button {:on-click clear-secrets} "Clear Secrets"]
    [:div
      [:textarea {:rows 3 :placeholder "enter a secret" :value (:secret @state) :on-change update-secret}]
      [:p (:secret @state)]
      [:button {:on-click post-secret} "Submit Secret"]]
    [:div
      [:input {:placeholder "enter a code" :value (:code @state) :on-change update-code}]
      [:p (:code @state)]
      [:button {:on-click get-music}  "Get Music By Code"]]])

(defn render-simple []
    (r/render [app]
      (.getElementById js/document "root")))

(render-simple)
