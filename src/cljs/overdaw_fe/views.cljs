(ns overdaw-fe.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
              [clojure.set :refer [union]]
              [clojure.string :as s]
              [overdaw-fe.config :refer [res notes from semitones]]))

(defn map-note [[idx result] {:keys [time duration amp]}]
  (let [time-idx (/ time res)
        new-idx (+ time-idx (/ duration res))]
    [new-idx (conj result [idx time-idx false amp] [time-idx new-idx true amp])]))

(defn map-row [times]
  (let [[end reduced] (reduce map-note [0 []] times)]
    (conj reduced [end notes false])))

(defn amp->class [amp]
  (condp > amp
    0.0 "a0"
    0.3 "a3"
    0.5 "a5"
    "a7"))

(defn time-panel []
  (fn []
    [:table.seq
     [:thead
      (into [:tr] (cons [:th {:class "key"} "x"]
                        (for [x (range notes)] [:th x])))]]))

(defn track-panel [instr]
  (let [part (re-frame/subscribe [:track-part instr])
        note-names (re-frame/subscribe [:notes])]
    (fn []
      [:table.seq
       (into ^{:key (str instr "-tbody")} [:tbody]
         (conj
           (for [y (reverse (range from (+ from semitones)))]
             (let [mapping (map-row (get @part y))
                   note (get @note-names (str y))]
               (into
                 ^{:key y} [:tr]
                 (cons
                   [:td.key (str y "/" note)]
                   (for [[from to has-note? amp] mapping]
                     (doall
                       (for [t (range from to)]
                         ^{:key (str t y)}
                         [:td {:class (s/join " " [(when has-note? "x") (amp->class amp)])
                               :on-click #(re-frame/dispatch [:edit-track [instr t y has-note?]])}
                          (if has-note? (when (= t from) note) "-")])))))
               ))))])))

(defn beat-panel []
  (let [beat (re-frame/subscribe [:beat])
        kit (re-frame/subscribe [:kit])]
    (fn []
      [:table.seq
       (into ^{:key "beat-tbody "} [:tbody]
         (for [instr (keys @kit)]
           (let [mapping (map-row (get @beat instr))]
             (into
               ^{:key instr} [:tr]
               (cons
                 [:td.key instr]
                 (for [[from to has-note? amp] mapping]
                   (doall
                     (for [t (range from to)]
                       ^{:key (str t instr)}
                       [:td {:class    (s/join " " [(when has-note? "x") (amp->class amp)])
                             :on-click #(re-frame/dispatch [:edit-beat [instr t has-note?]])}
                        "-"]))))))))])))

(defn control-panel []
  [:div
   [:button {:on-click #(re-frame/dispatch [:play])} "play"]
   [:button {:on-click #(re-frame/dispatch [:stop])} "stop"]
   [:button {:on-click #(re-frame/dispatch [:sync-track])} "sync"]])

(defn main-panel []
  (fn []
    [:div
     [:h2 "OverDAW"]
     [:hr]
     [:div.seq-container
      [time-panel]
      [:hr]
      [track-panel "wide-bass"]
      [:hr]
      [beat-panel]]
     [:hr]
     [control-panel]]))
