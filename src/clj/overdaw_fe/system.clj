(ns overdaw-fe.system
  (:require [com.stuartsierra.component :as component]
            [palikka.components.http-kit :as http-kit]
            [overdaw-fe.track :refer [track raw-track kit]]
            [overdaw-fe.handler :as handler]))

(defn new-system [config]
  (component/map->SystemMap
    {:state (reify component/Lifecycle
              (start [_] {:raw-track (ref raw-track)
                          :track     (ref (track raw-track))
                          :kit       (ref kit)}))
     :http  (component/using
              (http-kit/create
                (:http config)
                {:fn handler/create})
              [:state])}))
