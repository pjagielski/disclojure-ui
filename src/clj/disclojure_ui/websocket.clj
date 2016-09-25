(ns disclojure-ui.websocket
  (:require [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as t]
            [com.stuartsierra.component :as component]))

(defn get-broadcaster [sente]
  (fn [_ _ _ track]
    (doseq [uid (:any @(:connected-uids sente))]
      ((:chsk-send! sente) uid [:disclojure/track track]))))

(defrecord StateBroadcaster [state sente]
  component/Lifecycle
  (start [component]
    (let [ref (get state :raw-track)
          watch (add-watch ref :state-tracker (get-broadcaster sente))]
      (assoc component :watch watch)))
  (stop [component]
    (let [ref (get state :raw-track)]
      (remove-watch ref :state-tracker)
      (dissoc component :watch))))

(defn new-state-broadcaster []
  (map->StateBroadcaster {}))

(defn websocket-handler [{state :state}]
  (fn [{:keys [event] :as ev}]
    (t/debug "Received" event)))
