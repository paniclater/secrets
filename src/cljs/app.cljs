(ns compojore-secrets.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET]]))
(enable-console-print!)

(def state (r/atom {:secrets []}))

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

(defn clear-secrets []
  (swap! state (fn [a] (assoc a :secrets []))))

(defn app []
  [:div
    [:h2 "secrets"]
    [list-secrets]
    [:button {:on-click get-secrets} "Get Secrets"]
    [:button {:on-click clear-secrets} "Clear Secrets"]
    [:div
      [:input.form-control]]])


(defn render-simple []
  ; (get-secrets)
    (r/render [app]
      (.getElementById js/document "root")))

(render-simple)