(ns compojore-secrets.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))
(enable-console-print!)

(def state (r/atom {:secrets [{:text "secrets haven't been fetched yet"}] :secret ""}))

(defn list-secrets []
  [:ul
    (for [secret (:secrets @state)]
      ^{:key secret} [:li (:text secret)])])

(defn handler [response]
  (swap! state #(assoc % :secrets response)))
  ; (swap! state (fn [a] (assoc a :secrets response))))

(defn get-secrets []
  (GET "/secrets"
    {:response-format :json
      :keywords? true
      :handler handler}))
(defn post-secret []
  (POST "/secrets"
    {:format :json
     :response-format :json
     :params {:text (:secret @state)}
     :handler handler}))

(defn clear-secrets []
  (swap! state (fn [a] (assoc a :secrets []))))

(defn update-secret [event]
  (swap! state (fn [a] (assoc a :secret (-> event .-target .-value)))))

(defn app []
  [:div
    [:h2 "secrets"]
    [list-secrets]
    [:button {:on-click get-secrets} "Get Secrets"]
    [:button {:on-click clear-secrets} "Clear Secrets"]
    [:div
      [:textarea {:rows 3 :placeholder "enter a secret" :value (:secret @state) :on-change update-secret}]
      [:p (:secret @state)]
      [:p "hello"]
      [:button {:on-click post-secret} "Submit Secret"]]])


(defn render-simple []
    (r/render [app]
      (.getElementById js/document "root")))

(render-simple)