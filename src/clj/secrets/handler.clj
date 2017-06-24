(ns secrets.handler

  (:require [clojure.string :as string]
            [clojure.java.jdbc :as sql]
            [compojure.route :as route]
            [compojure.core :refer [GET POST PUT defroutes]]
            [hiccup.page :refer [include-js include-css html5]]
            [ring.util.response :refer [content-type file-response not-found resource-response response status]]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defn id-generator-factory []
  (let [new-id (atom 0)]
    (fn []
      (swap! new-id inc)
      @new-id)))
(def id-generator! (id-generator-factory))

(defn code []
  (let [names ["Billy" "Pilgrim" "Eichmann" "Angela"
               "Hoenikker" "Anita" "Elliot" "Rosewater"
               "Kilgore" "Trout" "Bertram" "Copeland"
               "Rumfoord" "Bryan" "Kelly" "Diana"
               "Moon" "Glampers" "Dorothy" "Roy"
               "Paul" "Proteus" "Wilbur" "Swain"
               "Dwayne" "Hoover" "Ed" "Finnerty"
               "Edgar" "Derby" "Edith" "Taft"
               "Edward" "McCabe" "Eliot" "Rosewater"
               "Felix" "Heonikker" "Francine" "Pefko"
               "Frank" "Wirtanen" "George" "Kraft"
               "Harold" "Ryan" "Harrison" "Bergeron"
               "Harry" "Nash" "Horlick" "Minton"
               "Howard" "Campbell" "Julian" "Castle"
               "Kilgore" "Trout" "Kurt" "Vonnegut"
               "Leon" "Trout" "Malachi" "Constant"
               "Marilee" "Kemp" "Mary" "O'Hare"
               "Melody" "Peterswald" "Montana" "Wildack"
               "Naomi" "Faust" "Nestor" "Aamons"
               "Paul" "Lazzaro" "Arthur" "Barnhouse"
               "Rabo" "Karabekian" "Roland" "Weary"]]
    (str (id-generator!)
         (loop [result []]
          (if (= (count result) 4)
            (string/join result)
            (recur
              (conj
                result
                (rand-nth names))))))))

(def pg-db
  {:dbtype "postgresql"
   :dbname "secrets"
   :host "localhost"
   :user "secrets"
   :password "LjYAbMGpFGdohDmJngx3aRyX"
   :stringtype "unspecified"})

(defn get-secrets [] (sql/query pg-db "select * from secrets"))

(defn get-music [code]
  (let [secrets (sql/query pg-db (str "select * from secrets where code = '" code "'"))
        secret (first secrets)
        secretStatus (:status secret)]
    (if (= secretStatus "APPROVED")
      (file-response "../Tarred_and_Pleasured_By_Agatha_Frisky.zip")
      (status (response {:error "NOT APPROVED"}) 400))))

(defn check-status [code]
  (let [secrets (sql/query pg-db (str "select * from secrets where code = '" code "'"))
        secret (first secrets)
        secretStatus (:status secret)]
    (println (concat "Secret status:  " secretStatus))
    (cond
      (= secretStatus "APPROVED") (response {:status "APPROVED"})
      (= secretStatus "PENDING")  (status (response {:status "PENDING"}) 202)
      (= secretStatus "REJECTED") (status (response {:status "REJECTED"}) 402)
      :else (not-found {:status "NOT FOUND"}))))

(defn add-secret [{text :text}]
  (let [new-code (code)]
    (sql/insert! pg-db :secrets
      {:code new-code :text text :status "PENDING"})
    {:code new-code}))

(defn update-secret [{id :id status :status}]
  (sql/update! pg-db :secrets
    {:status status} ["id = ?" id])
  (str status))

(defn get-status-for-secret [{code :code}]
  ({:code code}))

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
        [:meta {:name "viewport" :content "width=device-width"}]
        [:link {:rel "icon" :href "/key.ico" :type "image/x-icon"}]
        [:title "Agatha Frisky Exchanges Music For Secrets"]]
      [:div#root.container [:h3 "doing some html"]]
      (include-css "/index.css")
      (include-js "/index.js")))


  (GET "/.well-known/pki-validation/1713809fad5c4748b58ea109a0b7625c.txt" []
    (html5
      [:head
        [:meta {:charset "utf-8"}]
        [:title "Gettin that ole SSL on"]]
      [:div#root [:h3 "979675ff6d9c4513acbe8a8ae774d76f"]]))

  (GET  "/secrets" [] (response (get-secrets)))
  (GET "/secrets/:code" [code] (check-status code))
  (GET "/music/:code/Tarred_and_Pleasured_By_Agatha_Frisky.zip" [code] (get-music code))
  (POST "/secrets" {body :body} (response (add-secret body)))
  (PUT "/secrets" {body :body} (update-secret body))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
    (middleware/wrap-json-body {:keywords? true})
    (middleware/wrap-json-response)
    (wrap-defaults api-defaults)))
