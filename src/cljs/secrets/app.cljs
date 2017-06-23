;;Requirements
;;1. Submit a Secret, get confirmation back - [x]
;;2. Submit a Code, get Response - [x]
;; - If approved, get success response and download - [x] 200
;; - If denied,   get denied response - [x] (402 payment required)
;; - If pending,  get pending response - [x] (204 no content)
;;3. Encrypt Secrets in database - [ ]
;;4. Routes?
;;5. Styling - [ ]
;;6. Hide download button unless approved. - [ ]
;;6. Disable buttons if boxes are empty

(ns secrets.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))
(enable-console-print!)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STATE
(def state (r/atom {:secret ""
                    :code ""
                    :prompt ""
                    :show-download-link false}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; APP STATE HANDLERS
(defn update-secret [event]
  (swap! state #(assoc % :secret (-> event .-target .-value))))
(defn update-code [event]
  (swap! state #(assoc % :code (-> event .-target .-value))))
(defn update-prompt [new-prompt show-download-link]
  (println new-prompt show-download-link)
  (swap! state #(assoc % :prompt new-prompt :show-download-link show-download-link)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; HTTP REQUESTS AND HANDLERS
(defn post-secret-handler [response] (swap! state #(assoc % :prompt (str "Your code is " (:code response) ". Write it down somewhere safe!") :show-download-link false :secret "")))
(defn post-secret []
  (if (not (= "" (:secret @state)))
      (POST "/secrets"
        {:format :json
         :keywords? true
         :response-format :json
         :params {:text (:secret @state)}
         :handler post-secret-handler})
      (swap! state #(assoc % :prompt "Please enter a secret"))))

(defn check-secret-status-handler [response]
  (cond
    (= (:status response) "PENDING") (update-prompt "Your secret has not been reviewed yet, please check back soon!" false)
    (= (:status response) "APPROVED") (update-prompt "Your secret has been reviewed and found worthy!" true)
    :else (update-prompt "Uh oh, something went wrong, please email ryan@paniclater.com" false)))

(defn check-secret-status-error-handler [response]
  (let [status (:status response)]
    (if (= status 402)
      (update-prompt "Unfortunately your secret was weighed and found wanting, try again!" false)
      (update-prompt "Your code could not be found! Please try again." false))))

(defn check-secret-status []
  (if (not (= "" (:code @state)))
      (let [url (str "secrets/" (:code @state))]
        (GET url
          {:format :json
           :response-format :json
           :keywords? true
           :handler check-secret-status-handler
           :error-handler check-secret-status-error-handler}))
      (swap! state #(assoc % :prompt "Please enter a code"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REAGENT COMPONENTS
(defn secret-submitter []
  [:div.secret-submitter
    [:textarea.secret-submitter__textarea {:placeholder "enter a secret" :value (:secret @state) :on-change update-secret}]
    [:button.button {:class (when (= "" (:secret @state)) "disabled-button") :on-click post-secret} "submit secret"]])

(defn secret-checker []
  [:div
    [:input.secret-checker__input {:placeholder "enter a code" :value (:code @state) :on-change update-code}]
    [:button.button {:class (when (= "" (:code @state)) "disabled-button") :on-click check-secret-status}  "check status"]
    [:p.secret-checker__prompt (:prompt @state)]])

(defn download-link []
  [:div
   [:button.download-button
     [:a.download-link
       {:href (str "music/" (:code @state) "/Tarred_and_Pleasured_By_Agatha_Frisky.zip")
        :target "_blank"}
       "Your Secret is Approved, Click Here To Download!"]]])

(defn header []
  [:div.secrets-app__header
    [:h2 "How This Works:"]])
(defn instructions []
  [:div.secrets-app__instructions
    [:p "Write a secret and submit it."]
    [:p "Note the code you get back and keep it somewhere safe!"]
    [:p "Within 6 hours your secret will be reviewed. Come back, type your code in the check status box"]
    [:p "If it was approved, you will see a link to download Tarred And Pleasured, by Agatha Frisky"]
    [:p "If it is rejected, try again!"]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PARENT COMPONENT
(defn app []
  [:div.secrets-app
    [header]
    [instructions]
    [secret-submitter]
    (if (not (:show-download-link @state)) [secret-checker])
    (if (:show-download-link @state) [download-link])])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REAGENT MOUNT
(defn render-app []
    (r/render [app]
      (.getElementById js/document "root")))

(render-app)
