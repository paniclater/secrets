(ns compojore-secrets.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET]]))

(enable-console-print!)

(defn print-secrets [ok response]
  (if ok
    response
    (.error js/console (str response))))

(defn get-secrets []
  (GET "/secrets" {:response-format :json :handler print-secrets}))

; (defn secrets-list []
;   [:ul
;    (for [item items])])

(defn render-simple []
  (r/render [:div [:p "hello world"]]
    (.getElementById js/document "root")))

(render-simple)

