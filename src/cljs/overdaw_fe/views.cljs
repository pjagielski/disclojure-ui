(ns overdaw-fe.views
    (:require [re-frame.core :as re-frame]
              [clojure.set :refer [union]]
              [overdaw-fe.config :refer [res notes from semitones]]))

(defn map-note [[idx result] {:keys [time duration]}]
  (let [time-idx (/ time res)
        new-idx (+ time-idx (/ duration res))]
    [new-idx (conj result [idx time-idx false] [time-idx new-idx true])]))

(defn map-row [times]
  (let [[end reduced] (reduce map-note [0 []] times)]
    (conj reduced [end notes false])))

(defn track-panel [part]
  (let [part (re-frame/subscribe [:track-part part])
        note-names (re-frame/subscribe [:notes])]
    (fn []
      [:table.track
       (into ^{:key "tbody"} [:tbody]
         (conj
           (for [y (reverse (range from (+ from semitones)))]
             (let [mapping (map-row (get @part y))]
               (into
                 ^{:key y} [:tr]
                 (cons
                   [:td.key (str y "/" (get @note-names (str y)))]
                   (for [[from to has-note?] mapping]
                     (doall
                       (for [x (range from to)]
                         ^{:key (str x y)}
                         [:td {:class (when has-note? "x")}
                          (if has-note?
                            (when (= x from) (get @note-names (str y))) "-")])))))))
           (into [:tr] (cons [:td "x"] (for [x (range notes)] [:td x])))))])))

(defn beat-panel []
  (let [beat (re-frame/subscribe [:beat])]
    (fn []
      [:table.track
       (into ^{:key "beat-tbody"} [:tbody]
         (for [y ["kick" "snare"]]
           (let [mapping (map-row (get @beat y))]
             (into
               ^{:key y} [:tr]
               (cons
                 [:td.key y]
                 (for [[from to has-note?] mapping]
                   (doall
                     (for [x (range from to)]
                       ^{:key (str x y)}
                       [:td {:class (when has-note? "x")}
                        "-"]))))))))])))

(defn control-panel []
  [:div
   [:button {:on-click #(re-frame/dispatch [:play])} "play"]
   [:button {:on-click #(re-frame/dispatch [:stop])} "stop"]
   [:button {:on-click #(re-frame/dispatch [:sync-track])} "sync"]])

(defn main-panel []
  (fn []
    [:div
     [:div "OverDAW"]
     [track-panel "supersaw"]
     [:hr]
     [beat-panel]
     [:hr]
     [control-panel]]))
