(ns overdaw-fe.system
  (:require [com.stuartsierra.component :as component]
            [palikka.components.http-kit :as http-kit]
            [overdaw-fe.track :refer [raw-track]]
            [overdaw-fe.handler :as handler]))

(defn new-system [config]
  (component/map->SystemMap
    {:track (reify component/Lifecycle
              (start [_] {:track (ref raw-track)}))
     :http  (component/using
              (http-kit/create
                (:http config)
                {:fn
                 (if (:dev-mode? config)
                   ; re-create handler on every request
                   (fn [system] #((handler/create system) %))
                   handler/create)})
              [:track])}))
