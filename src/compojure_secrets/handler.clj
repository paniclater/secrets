(ns compojure-secrets.handler
            
  (:require [clojure.string :as string] 
            [clojure.java.jdbc :as sql]
            [compojure.route :as route]
            [compojure.core :refer [GET POST PUT defroutes]]
            [ring.util.response :refer [content-type resource-response response]]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))
            

(defn code []
  (loop [result []]
    (if (= (count result) 4)
      (string/join result)
      (recur
        (conj
          result
          (rand-nth
            ["Billy" "Pilgrim" "Eichmann" "Angela" "Hoenikker" "Anita" "Elliot" "Rosewater" "Kilgore" "Trout" "Bertram" "Copeland" "Rumfoord"]))))))

(def pg-db 
  {:dbtype "postgresql"
   :dbname "secrets"
   :host "localhost"
   :user "ryanmoore"
   :password "J2gnFFrudQLmnGbiCUiDEzWh"
   :stringtype "unspecified"})

(defn get-secret-for-code []
  (sql/query pg-db "select * from secrets"))

(defn add-secret [{secret :secret}]
  (sql/insert! pg-db :secrets
    {:code (code) :secret secret :status "PENDING"})
  (code))

(defn update-secret [{id :id status :status}]
  (sql/update! pg-db :secrets
    {:status status} ["id = ?" id])
    (str status))

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
  (GET  "/secrets" [] (response (get-secret-for-code)))
  (POST "/secrets" {body :body} (add-secret body))
  (PUT "/secrets" {body :body} (update-secret body))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
    (middleware/wrap-json-body {:keywords? true})
    (middleware/wrap-json-response)
    (wrap-defaults api-defaults)))
