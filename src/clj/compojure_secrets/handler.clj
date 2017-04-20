(ns compojure-secrets.handler

  (:require [clojure.string :as string]
            [clojure.java.jdbc :as sql]
            [compojure.route :as route]
            [compojure.core :refer [GET POST PUT defroutes]]
            [hiccup.page :refer [include-js include-css html5]]
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
   :user "postgres"
  ;  :password "J2gnFFrudQLmnGbiCUiDEzWh"
   :stringtype "unspecified"})

(defn get-secrets []
  (sql/query pg-db "select * from secrets"))

(defn add-secret [{text :text}]
  (sql/insert! pg-db :secrets
    {:code (code) :text text :status "PENDING"})
  (code))

(defn update-secret [{id :id status :status}]
  (sql/update! pg-db :secrets
    {:status status} ["id = ?" id])
  (str status))

(defn get-music [{code :code}]
  (sql/query pg-db
    (str "select status from secrets where code = '" code "'")))

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
  (GET "/" []
    (html5
      [:head
        [:meta {:charset "utf-8"}]
        [:title "my first clojurescript"]]
      [:div#root [:h3 "doing some html"]]
      (include-js "/index.js")))

  (GET  "/secrets" [] (response (get-secrets)))
  (POST "/secrets" {body :body} (add-secret body))
  (PUT "/secrets" {body :body} (update-secret body))
  (GET "/music" {params :params} (get-music params))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
    (middleware/wrap-json-body {:keywords? true})
    (middleware/wrap-json-response)
    (wrap-defaults api-defaults)))