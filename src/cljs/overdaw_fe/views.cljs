(ns overdaw-fe.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [clojure.set :refer [union]]
            [clojure.string :as s]
            [overdaw-fe.config :refer [res ticks semitones]]))

(defn map-note [[idx result] {:keys [time duration amp]}]
  (let [time-idx (/ time res)
        new-idx (+ time-idx (/ duration res))]
    [new-idx (conj result [idx time-idx false amp] [time-idx new-idx true amp])]))

(defn map-row [times]
  (let [[end reduced] (reduce map-note [0 []] times)]
    (conj reduced [end ticks false])))

(defn amp->class [amp]
  (condp > amp
    0.0 "a0"
    0.3 "a3"
    0.5 "a5"
    "a7"))

(defn sep? [x] (= 0 (mod x 8)))

(defn would-have-note [note time cursor-pos controls]
    (and (= note (:note @cursor-pos))
         (<= (:time @cursor-pos) time)
         (< time (+ (:time @cursor-pos) (/ (get @controls :duration) res)))))

(defn time-panel []
  (fn []
    [:table.seq
     [:thead
      (into [:tr]
        (cons [:th {:class "key"} "x"]
          (for [x (range ticks)]
            [:td {:class (when (sep? x) "t")} x])))]]))

(defn track-panel [instr]
  (let [part (re-frame/subscribe [:track-part instr])
        note-names (re-frame/subscribe [:notes])
        controls (re-frame/subscribe [:track-controls])
        cursor-pos (reagent.core/atom {})]
    (fn []
      (let [from (:from @controls)]
        [:table.seq
         (into ^{:key (str instr "-tbody")} [:tbody]
           (conj
             (for [y (reverse (range from (+ from semitones)))]
               (let [mapping (map-row (get @part y)) note (get @note-names (str y))]
                 (into
                   ^{:key y} [:tr]
                   (cons
                     [:td.key (str y "/" note)]
                     (for [[from to has-note? amp] mapping]
                       (doall
                         (for [t (range from to)]
                           ^{:key (str t y)}
                           [:td {:class    (s/join " " [(when has-note? "x") (amp->class amp)
                                                        (when (would-have-note y t cursor-pos controls) "p")
                                                        (when (sep? t) "t")])
                                 :on-click #(re-frame/dispatch [:edit-track [instr t y has-note?]])
                                 :on-mouse-enter #(reset! cursor-pos {:note y :time t})
                                 :on-mouse-leave #(reset! cursor-pos {})}
                            (if has-note? (when (= t from) note) "-")])))))))))]))))

(defn beat-panel []
  (let [beat (re-frame/subscribe [:beat])
        kit (re-frame/subscribe [:kit])]
    (fn []
      [:table.seq
       (into ^{:key "beat-tbody"} [:tbody]
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
                       [:td {:class    (s/join " " [(when has-note? "x") (amp->class amp)
                                                    (when (sep? t) "t")])
                             :on-click #(re-frame/dispatch [:edit-beat [instr t has-note?]])}
                        "-"]))))))))])))

(defn update-control [key pred f t controls]
  (fn []
    (let [val (get @controls key)]
      [:button.btn
       {:on-click #(when (pred val)
                    (re-frame/dispatch [:change-track-control [key (f val)]]))}
       t])))

(defn track-control [key values controls]
  (fn []
    (into [:select.form-control
           {:name      (str key) :value (get @controls key)
            :on-change (fn [e] (re-frame/dispatch [:change-track-control
                                                   [key (js/Number (.. e -target -value))]]))}]
          (map (fn [x] [:option {:value x} x]) values))))

(defn track-control-panel []
  (let [controls (re-frame/subscribe [:track-controls])]
    (fn []
      [:div.track-controls.form-inline
       [:div.row
        [:div.col-md-12
         [update-control :from #(> % 24) dec "v" controls]
         [track-control :from (range 24 72) controls]
         [update-control :from #(< % 72) inc "^" controls]]]
       [:div.row
        [:div.col-md-12
         [update-control :duration #(> % 0.25) #(- % 0.25) "v" controls]
         [track-control :duration (range 0.25 4 0.25) controls]
         [update-control :duration #(< % 4) #(+ % 0.25) "^" controls]]]])))

(defn control-button [name event]
  [:button.form-control.btn-small {:on-click #(re-frame/dispatch [event])} name])

(defn play-control-panel []
  (fn []
    [:div.form-inline
     [control-button "play" :play]
     [control-button "stop" :stop]
     [control-button "sync" :sync-track]]))

(defn main-panel []
  (fn []
    [:div.main
     [:div.row
      [:h2 "OverDAW"]]
     [:div.row
      [:div.col-md-10
       [:div.seq-container
        [time-panel]
        [:hr]
        [track-panel "wide-bass"]
        [:hr]
        [beat-panel]]]
      [:div.col-md-2
       [track-control-panel]]]
     [:div.row.play-controls
      [play-control-panel]]]))
