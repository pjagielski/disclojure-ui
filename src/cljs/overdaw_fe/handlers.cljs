(ns overdaw-fe.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :as re-frame]
            [overdaw-fe.db :as db]
            [overdaw-fe.config :as c]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (go
      (let [response (<! (http/get (str c/api-base "/track")))]
        (re-frame/dispatch [:sync-track (:body response)])))
    db/default-db))

(re-frame/register-handler
  :sync-track
  (fn [db [_ track]]
    (assoc db :track track)))

(re-frame/register-handler
  :change-name
  (fn [db [_ value]]
    (assoc db :name value)))
