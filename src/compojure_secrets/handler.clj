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

(defn add-secret [{text :text}]
  (sql/insert! pg-db :secrets
    {:code (code) :text text :status "PENDING"})
  (code))

(defn update-secret [{id :id status :status}]
  (sql/update! pg-db :secrets
    {:status status} ["id = ?" id])
  (str status))

(def create-table
  (sql/create-table-ddl
    :secrets
    [[:id "serial" "PRIMARY KEY"]
     [:code "text"]
     [:text "text"]
     [:status "text"]]))

(def secrets-table-created?
  (->
    (sql/query pg-db
      [(str "select count(*) from information_schema.tables where table_name='secrets'")])
    first :count pos?))

(when (not secrets-table-created?)
  (sql/db-do-commands pg-db create-table))

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
