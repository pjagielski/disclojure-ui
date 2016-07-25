(ns overdaw-fe.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [overdaw-fe.handlers]
              [overdaw-fe.subs]
              [overdaw-fe.websocket]
              [overdaw-fe.views :as views]
              [overdaw-fe.config :as config]))

(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init [] 
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
