(ns overdaw-fe.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [clojure.set :refer [union]]
            [clojure.string :as s]
            [overdaw-fe.config :refer [res semitones]]
            [shodan.console :include-macros true]))

(defn map-note [[idx result] {:keys [time duration amp]}]
  (let [time-idx (/ time res)
        new-idx (+ time-idx (/ duration res))]
    [new-idx (conj result [idx time-idx false amp] [time-idx new-idx true amp])]))

(defn map-row [times ticks]
  (let [[end reduced] (reduce map-note [0 []] times)]
    (conj reduced [end ticks false])))

(defn amp->class [amp]
  (condp > amp
    0.0 "a0"
    0.3 "a3"
    0.5 "a5"
    "a7"))

(defn sep? [x] (= 0 (mod x 8)))

(defn would-have-note [note time cursor-pos duration]
    (and (= note (:note @cursor-pos))
         (<= (:time @cursor-pos) time)
         (< time (+ (:time @cursor-pos) (/ duration res)))))

(defn ticks [bars]
  (* 8 bars))

(defn time-panel []
  (let [controls (re-frame/subscribe [:track-controls])]
    (fn []
      [:table.seq
       [:thead
        (into [:tr]
          (cons [:th {:class "key"} "x"]
            (for [x (range (ticks (get @controls :bars)))]
              [:td {:class (when (sep? x) "t")} x])))]])))


(defn track-panel []
  (let [controls (re-frame/subscribe [:track-controls])
        part (re-frame/subscribe [:track-part])
        note-names (re-frame/subscribe [:notes])
        cursor-pos (reagent.core/atom {})]
    (fn []
      (let [from (:from @controls) instr (:instr @controls)]
        [:table.seq
         (into ^{:key (str instr "-tbody")} [:tbody]
           (conj
             (for [y (reverse (range from (+ from semitones)))]
               (let [mapping (map-row (get @part y) (ticks (get @controls :bars)))
                     note (get @note-names (str y))
                     duration (get @controls :duration)]
                 (into
                   ^{:key y} [:tr]
                   (cons
                     [:td.key {:on-click #(re-frame/dispatch [:play-note {:instr instr :duration duration :note y}])}
                      (str y "/" note)]
                     (for [[from to has-note? amp] mapping]
                       (doall
                         (for [t (range from to)]
                           ^{:key (str t y)}
                           [:td {:class    (s/join " " [(when has-note? "x") (amp->class amp)
                                                        (when (would-have-note y t cursor-pos duration) "p")
                                                        (when (sep? t) "t")])
                                 :on-click #(re-frame/dispatch [:edit-track [instr t y has-note?]])
                                 :on-mouse-enter #(reset! cursor-pos {:note y :time t})
                                 :on-mouse-leave #(reset! cursor-pos {})}
                            (if has-note? (when (= t from) note) "-")])))))))))]))))

(defn beat-panel []
  (let [beat (re-frame/subscribe [:beat])
        kit (re-frame/subscribe [:kit])
        controls (re-frame/subscribe [:track-controls])]
    (fn []
      [:table.seq
       (into ^{:key "beat-tbody"} [:tbody]
         (for [instr (keys @kit)]
           (let [mapping (map-row (get @beat instr) (ticks (get @controls :bars)))]
             (into
               ^{:key instr} [:tr]
               (cons
                 [:td.key {:on-click #(re-frame/dispatch [:play-kit instr])}
                  instr]
                 (for [[from to has-note? amp] mapping]
                   (doall
                     (for [t (range from to)]
                       ^{:key (str t instr)}
                       [:td {:class    (s/join " " [(when has-note? "x") (amp->class amp)
                                                    (when (sep? t) "t")])
                             :on-click #(re-frame/dispatch [:edit-beat [instr t has-note?]])}
                        "-"]))))))))])))

(defn slider [key min max step controls]
  (let [instr (reaction (get @controls :instr))]
    (fn []
      [:input.track-control
       {:type      "range"
        :min       min
        :max       max
        :step      step
        :value     (get-in @controls [@instr key])
        :on-change (fn [e] (re-frame/dispatch [:change-instr-control
                                               [@instr key (js/Number (-> e .-target .-value))]]))}])))

(defn step-control [key pred f t controls]
  (fn []
    (let [val (get @controls key)]
      [:button.btn
       {:on-click #(when (pred val)
                    (re-frame/dispatch [:change-track-control [key (f val)]]))}
       t])))

(defn track-control [key values-fn controls f]
  (fn []
    (into [:select.form-control.track-control
           {:name      (str key) :value (get @controls key)
            :on-change (fn [e] (re-frame/dispatch [:change-track-control
                                                   [key (f (.. e -target -value))]]))}]
          (map (fn [x] [:option {:value x} x]) (values-fn)))))

(defn update-control-panel [key start end step controls]
  (fn []
    [:div.row
     [:div.col-md-12
      [step-control key #(> % start) #(- % step) "v" controls]
      [track-control key #(range start end step) controls js/Number]
      [step-control key #(< % end) #(+ % step) "^" controls]]]))

(defn track-control-panel []
  (let [controls (re-frame/subscribe [:track-controls])
        instruments (re-frame/subscribe [:instruments])
        instr (reaction (:instr @controls))]
    (fn []
      [:div.track-controls.form-inline
       [update-control-panel :from 24 72 1 controls]
       [update-control-panel :duration 0.25 4.25 0.25 controls]
       [track-control :instr #(deref instruments) controls identity]
       [slider :cutoff 100 5000 50 controls]])))

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
      [:div.col-md-7
       [:div.seq-container
        [time-panel]
        [:hr]
        [track-panel]
        [:hr]
        [beat-panel]
        [:hr]]]
      [:div.col-md-5
       [track-control-panel]]]
     [:div.row.play-controls
      [play-control-panel]]]))
