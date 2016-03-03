(ns overdaw-fe.system
  (:require [com.stuartsierra.component :as component]
            [palikka.components.http-kit :as http-kit]
            [overdaw-fe.track :refer [track raw-track kit]]
            [overdaw-fe.play :refer [controls]]
            [overdaw-fe.handler :as handler]))

(defn new-system [config]
  (component/map->SystemMap
    {:state (reify component/Lifecycle
              (start [_] {:raw-track raw-track
                          :track     (ref (track @raw-track))
                          :kit       kit
                          :controls  controls}))
     :http  (component/using
              (http-kit/create
                (:http config)
                {:fn handler/create})
              [:state])}))
