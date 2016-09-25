(ns disclojure-ui.system
  (:require [com.stuartsierra.component :as component]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [taoensso.sente.server-adapters.http-kit :refer [http-kit-adapter]]
            (system.components
              [sente :refer [new-channel-socket-server sente-routes]]
              [endpoint :refer [new-endpoint]]
              [handler :refer [new-handler]]
              [http-kit :refer [new-web-server]]
              [middleware :refer [new-middleware]])
            (disclojure
              [play :refer [controls]]
              [live :refer [state reset-track]]
              [kit :refer [kit]])
            (disclojure-ui
              [track :refer [initial-track]]
              [api :refer [api-routes]]
              [websocket :refer [websocket-handler new-state-broadcaster]])))

(defn new-system [config]
  (component/map->SystemMap

    {:state          (reify component/Lifecycle
                       (start [_]
                         (reset-track initial-track)
                         {:raw-track (get state :raw-track)
                          :kit       kit
                          :controls  controls}))

     :api            (component/using
                       (new-endpoint api-routes)
                       [:state])

     :sente          (component/using
                       (new-channel-socket-server websocket-handler
                                                  http-kit-adapter {:wrap-component? true})
                       [:state])

     :sente-endpoint (component/using
                       (new-endpoint sente-routes)
                       [:sente])

     :middleware     (new-middleware {:middleware [[wrap-defaults :defaults]]
                                      :defaults   api-defaults
                                      :not-found  "<h2>The requested page does not exist.</h2>"})

     :handler        (component/using
                       (new-handler)
                       [:sente-endpoint :api :middleware])

     :http           (component/using
                       (new-web-server
                         (get-in config [:http :port]))
                       [:handler])

     :broadcaster    (component/using
                       (new-state-broadcaster)
                       [:state :sente])
     }))
