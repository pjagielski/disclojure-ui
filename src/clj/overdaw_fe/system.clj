(ns overdaw-fe.system
  (:require [com.stuartsierra.component :as component]
            [palikka.components.http-kit :as http-kit]
            [disclojure.play :refer [controls]]
            [disclojure.live :refer [state reset-track]]
            [overdaw-fe.track :refer [initial-track]]
            [disclojure.kit :refer [kit]]
            [overdaw-fe.handler :as handler]))

(defn new-system [config]
  (component/map->SystemMap
    {:state (reify component/Lifecycle
              (start [_]
                (reset-track initial-track)
                {:raw-track (get state :raw-track)
                 :kit       kit
                 :controls  controls}))
     :http  (component/using
              (http-kit/create
                (:http config)
                {:fn handler/create})
              [:state])}))
