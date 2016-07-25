(ns overdaw-fe.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :as re-frame]
            [plumbing.core :refer [map-vals map-keys]]
            [cljs.core.async :refer [<!]]
            [ajax.core :refer [GET POST PUT]]
            [overdaw-fe.db :as db]
            [overdaw-fe.config :as c :refer [res]]))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (re-frame/dispatch [:sync-track])
    (re-frame/dispatch [:sync-kit])
    (re-frame/dispatch [:sync-instruments])
    (re-frame/dispatch [:sync-notes [24 96]])
    db/default-db))

(re-frame/register-handler
  :sync-track
  (fn [db _]
    (GET (str c/api-base "/track")
         {:response-format :json :keywords? true
          :handler #(re-frame/dispatch [:sync-track-response (js->clj %1)])})
    db))

(re-frame/register-handler
  :sync-track-response
  (fn [db [_ track]]
    (let [result (->> track
                      (map-keys name)
                      (map-vals #(group-by :pitch %)))]
      (println (get result "beat")))
    (->> track
         (map-keys name)
         (map-vals #(group-by :pitch %))
         (assoc db :track))))

(defn keywords->names [notes]
  (->> notes
       (map (fn [{:keys [drum part] :as n}]
              (as-> n $
                    (when drum (assoc $ :drum (name drum)))
                    (when part (assoc $ :part (name part))))))))

(re-frame/register-handler
  :push-track
  (fn [db [_ track]]
    (let [result (->> track
                      (map-keys name)
                      (map-vals keywords->names)
                      (map-vals #(group-by :pitch %)))]
      (println (get result "beat")))
    (->> track
         (map-keys name)
         (map-vals keywords->names)
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
  :sync-instruments
  (fn [db _]
    (GET (str c/api-base "/instruments")
         {:response-format :json
          :handler #(re-frame/dispatch [:sync-instruments-response (js->clj %1)])})
    db))

(re-frame/register-handler
  :sync-instruments-response
  (fn [db [_ instruments]]
    (assoc db :instruments instruments)))

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
  :change-control
  (fn [db [_ [name value]]]
    (assoc-in db [:controls name] value)))

(re-frame/register-handler
  :change-editor-control
  (fn [db [_ [name value]]]
    (assoc-in db [:editor name] value)))

(re-frame/register-handler
  :change-instr-control
  (fn [db [_ [instr control value]]]
    (go
      (PUT (str c/api-base "/controls")
           {:params {:instr instr :control control :value value} :format :json}))
    (assoc-in db [:instr-controls instr control] value)))

(re-frame/register-handler
  :play-note
  (fn [db [_ note]]
    (let [params (assoc note :duration (db/duration db))]
      (POST (str c/api-base "/instruments/play")
            {:params params :format :json}))
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
          time (* t-idx res) const (db/editor db)
          new-entry (merge const {:time time :pitch note :part instr})]
      (go
        (PUT (str c/api-base "/track")
             {:params (merge new-entry {:type (if has-note? :remove :add)})
              :format :json}))
      (->> (if has-note?
             (remove #(and (= time (:time %)) (= note (:pitch %))) curr-pattern)
             (conj curr-pattern new-entry))
           (sort-by :time)
           (assoc-in db path)))))
