(ns overdaw-fe.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :as re-frame]
            [plumbing.core :refer [map-vals]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [ajax.core :refer [GET POST]]
            [overdaw-fe.db :as db]
            [overdaw-fe.config :as c :refer [res]]))

(defn- wait-and-dispatch [ch event]
  (go (let [response (<! ch)]
        (re-frame/dispatch-sync [event (:body response)]))))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (re-frame/dispatch [:sync-track])
    (re-frame/dispatch [:sync-notes [60 96]])
    db/default-db))

(re-frame/register-handler
  :sync-track
  (fn [db _]
    (wait-and-dispatch (http/get (str c/api-base "/track"))
                       :sync-track-response)
    db))

(re-frame/register-handler
  :sync-track-response
  (fn [db [_ track]]
    (->> track
         (group-by :part)
         (map-vals #(group-by :pitch %))
         (assoc db :track))))

(re-frame/register-handler
  :sync-notes
  (fn [db [_ [from to]]]
    (GET (str c/api-base "/notes/" from "/" to)
        {:response-format :json :keywords? false
         :handler #(re-frame/dispatch [:sync-notes-response (js->clj %1)])})
    db))

(re-frame/register-handler
  :sync-notes-response
  (fn [db [_ notes]]
    (assoc db :notes notes)))

(re-frame/register-handler
  :play
  (fn [db _]
    (POST (str c/api-base "/play"))
    db))

(re-frame/register-handler
  :stop
  (fn [db _]
    (POST (str c/api-base "/stop"))
    db))

(re-frame/register-handler
  :change-name
  (fn [db [_ value]]
    (assoc db :name value)))
