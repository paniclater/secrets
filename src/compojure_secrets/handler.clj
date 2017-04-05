(ns compojure-secrets.handler
            
  (:require [clojure.java.jdbc :as sql]
            [compojure.route :as route]
            [compojure.core :refer [GET POST defroutes]]
            [ring.util.response :refer [content-type resource-response response]]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))
            
(def pg-db 
  {:dbtype "postgresql"
   :dbname "secrets"
   :host "localhost"
   :user "ryanmoore"
   :password "J2gnFFrudQLmnGbiCUiDEzWh"
   :stringtype "unspecified"})

(defn get-secret-for-code []
  (sql/query pg-db "select * from secrets"))

(defn add-secret [request]
  (sql/insert! pg-db :secrets
    {:code "code" :secret "secret" :status "PENDING"})
  (str request))

  ; TODO::
  ; -> initialize secrets table with id, code, secret and reviewed columns
  ; -> id auto incrementing
  ; -> code, secret simple text fields
  ; -> reviewed: enum "PENDING", "APPROVED", "REJECTED"
; (defn init-db []
;   (sql/create-table-ddl
;     :secrets
;     [:id "serial" "PRIMARY KEY"]
;     [:code "text"]
;     [:secret_text "text"]
;     [:reviewed "text"]))

(defroutes app-routes
  (GET "/" [] (content-type (resource-response "index.html" {:root "public"}) "text/html"))
  (GET  "/widgets" [] (response [{:name "Widget 1"} {:name "Widget 2"}]))
  (POST "/secrets" {params :params body :body} (response body))
  ; (GET "/secrets" [] ()
  ;   (str "getting secrets: " (get-secret-for-code)))
  ; (POST "/secrets" [] ()
  ;   (str "posting secrets: " (add-secret)))
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (-> app-routes
    (middleware/wrap-json-body {:keywords? true})
    (middleware/wrap-json-response)
    (wrap-defaults api-defaults)))
