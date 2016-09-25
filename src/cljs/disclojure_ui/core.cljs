(ns disclojure-ui.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [disclojure-ui.handlers]
              [disclojure-ui.subs]
              [disclojure-ui.websocket]
              [disclojure-ui.views :as views]
              [disclojure-ui.config :as config]))

(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init [] 
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
