(ns overdaw-fe.views
    (:require [re-frame.core :as re-frame]))

(defn note-render []
  (fn [{:keys [time duration]}]
    [:div time duration]))

(defn track-panel []
  (let [track (re-frame/subscribe [:track])]
    (fn []
      [:div
       (map-indexed
         (fn [k v] ^{:key k} [note-render v])
         @track)])))

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div
       [:div "Hello from " @name]
       [track-panel]])))
