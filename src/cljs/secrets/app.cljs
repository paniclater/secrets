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
;; HTTP REQUESTS AND HANDLERS
(defn post-secret-handler [response] (swap! state #(assoc % :prompt (str "Your code is " (:code response) ". Please write it down somewhere safe and check back in about 6 hours to see if it is approved!") :show-download-link false)))
(defn post-secret []
  (POST "/secrets"
    {:format :json
     :keywords? true
     :response-format :json
     :params {:text (:secret @state)}
     :handler post-secret-handler}))

(defn check-secret-status-handler [response]
  (cond
    (= (:status response) "PENDING") (update-prompt "Your secret has not been reviewed yet, please check back soon! We endeavor to review all secrets within 6 hours of submission" false)
    (= (:status response) "APPROVED") (update-prompt "Your secret has been reviewed and found worthy!" true)
    :else (update-prompt "Uh oh, something went wrong, please email ryan@paniclater.com" false)))

(defn check-secret-status-error-handler [response]
  (let [status (:status response)]
    (if (= status 402)
      (update-prompt "Unfortunately your secret was weighed and found wanting, probably because you spammed the input or are a bot. Try again!" false)
      (update-prompt "Uh oh, something went wrong, please email ryan@paniclater.com" false))))

(defn check-secret-status []
  (let [url (str "secrets/" (:code @state))]
    (GET url
      {:format :json
       :response-format :json
       :keywords? true
       :handler check-secret-status-handler
       :error-handler check-secret-status-error-handler})))

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
;; REAGENT COMPONENTS
(defn secret-submitter []
  [:div.secret-submitter
    [:textarea.secret-submitter__textarea {:placeholder "enter a secret" :value (:secret @state) :on-change update-secret}]
    [:button.button {:on-click post-secret} "submit secret"]])

(defn secret-checker []
  [:div
    [:input.secret-checker__input {:placeholder "enter a code" :value (:code @state) :on-change update-code}]
    [:button.button {:on-click check-secret-status}  "check status"]
    [:p.secret-checker__prompt (:prompt @state)]])

(defn download-link []
  [:div
   [:button.download-button
     [:a.download-link
       {:href (str "music/" (:code @state) "/agatha-frisky.zip")
        :target "_blank"}
       "Your Secret is Approved, Click Here To Download!"]]])

(defn header []
  [:div.secrets-app__header
    [:h2 "How This Works:"]])
(defn instructions []
  [:div.secrets-app__instructions
    [:p "You write a secret in the text box below and submit it."]
    [:p "The secret will be submitted to Agatha Frisky and you will see a code below"]
    [:p "Keep the code! Write it somewhere safe! Email it to yourself"]
    [:p "Within 6 hours your secret will be reviewed and you can come back, type your code in the box and find out if it was approved or rejected"]
    [:p "If it was approved, you will see a link to download Tarred And Pleasured, the latest Agatha Frisky album"]
    [:p "If it is rejected, try another! Most likely it was rejected because you are a bot or tried to spam the input. So Don't Do That."]])


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
