(ns overdaw-fe.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :as re-frame]
            [plumbing.core :refer [map-vals]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [ajax.core :refer [GET POST PUT]]
            [overdaw-fe.db :as db]
            [overdaw-fe.config :as c :refer [res]]))

(defn- wait-and-dispatch [ch event]
  (go (let [response (<! ch)]
        (re-frame/dispatch-sync [event (:body response)]))))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (re-frame/dispatch [:sync-track])
    (re-frame/dispatch [:sync-kit])
    (re-frame/dispatch [:sync-notes [24 96]])
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
  :sync-kit
  (fn [db _]
    (GET (str c/api-base "/kit")
         {:response-format :json :keywords? false
          :handler #(re-frame/dispatch [:sync-kit-response (js->clj %1)])})
    db))

(re-frame/register-handler
  :sync-kit-response
  (fn [db [_ kit]]
    (assoc db :kit (into (sorted-map) kit))))

(re-frame/register-handler
  :play
  (fn [db _]
    (POST (str c/api-base "/track/play"))
    db))

(re-frame/register-handler
  :stop
  (fn [db _]
    (POST (str c/api-base "/track/stop"))
    db))

(re-frame/register-handler
  :change-track-control
  (fn [db [_ [name value]]]
    (assoc-in db [:track-controls name] value)))

(re-frame/register-handler
  :play-note
  (fn [db [_ note]]
    (POST (str c/api-base "/instruments/play")
          {:params note :format :json})
    db))

(re-frame/register-handler
  :play-kit
  (fn [db [_ drum]]
    (POST (str c/api-base "/kit/play")
          {:params {:drum drum} :format :json})
    db))

(re-frame/register-handler
  :edit-beat
  (fn [db [_ [instr t-idx has-note?]]]
    (let [path [:track "beat" nil] curr-pattern (get-in db path [])
          time (* t-idx res) const (:beat-controls db)
          new-entry (merge const {:time time :duration (- 8 time) :drum instr})]
      (PUT (str c/api-base "/beat")
           {:params (merge new-entry {:type (if has-note? :remove :add)})
            :format :json})
      (->> (if has-note?
             (remove #(and (= time (:time %)) (= instr (:drum %))) curr-pattern)
             (conj curr-pattern new-entry))
           (sort-by :time)
           (assoc-in db path)))))

(re-frame/register-handler
  :edit-track
  (fn [db [_ [instr t-idx note has-note?]]]
    (let [path [:track instr note] curr-pattern (get-in db path [])
          time (* t-idx res) const (:track-controls db)
          new-entry (merge (select-keys const [:duration :amp])
                           {:time time :pitch note :part instr})]
      (PUT (str c/api-base "/track")
           {:params (merge new-entry {:type (if has-note? :remove :add)})
            :format :json})
      (->> (if has-note?
             (remove #(and (= time (:time %)) (= note (:pitch %))) curr-pattern)
             (conj curr-pattern new-entry))
           (sort-by :time)
           (assoc-in db path)))))
