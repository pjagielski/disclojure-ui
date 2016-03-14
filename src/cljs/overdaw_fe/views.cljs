(ns overdaw-fe.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [clojure.set :refer [union]]
            [clojure.string :as s]
            [clojure.data :as d]
            [overdaw-fe.config :refer [res]]
            [shodan.console :include-macros true]
            [reagent.core :as reagent]))

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

(defn track-row [instr y bars duration]
  (let [part (re-frame/subscribe [:track-part instr y])
        note-names (re-frame/subscribe [:notes])
        cursor-pos (reagent.core/atom {})]
    (reagent/create-class
      {:should-component-update
       (fn [_ _ _] false)
       :reagent-render
       (fn [instr y bars duration]
         (let [mapping (map-row @part (ticks bars))
               note (get @note-names (str y))]
           (into
             ^{:key y} [:tr]
             (cons
               [:td.key {:on-click #(re-frame/dispatch [:play-note {:instr instr :note y}])}
                note]
               (for [[from to has-note? amp] mapping]
                 (doall
                   (for [t (range from to)]
                     ^{:key (str t y)}
                     [:td {:class          (s/join " " [(when has-note? "x") (amp->class amp)
                                                        (when (would-have-note y t cursor-pos duration) "p")
                                                        (when (sep? t) "t")])
                           :on-click       #(re-frame/dispatch [:edit-track [instr t y has-note?]])
                           :on-mouse-enter #(reset! cursor-pos {:note y :time t})
                           :on-mouse-leave #(reset! cursor-pos {})}
                      (if has-note? (when (= t from) note) "-")])))))))})))

(defn track-panel []
  (let [configs (re-frame/subscribe [:configs])
        controls (re-frame/subscribe [:controls])
        editor (re-frame/subscribe [:editor])]
    (fn []
      (println @editor @controls)
      (time
        [:div.track-container
         (let [{:keys [from to bars]} @configs
               {:keys [instr]} @controls
               {:keys [duration]} @editor]
           [:table.seq
            (into ^{:key (str instr "-tbody")} [:tbody]
              (conj
                (for [y (reverse (range from to))]
                  [track-row instr y bars duration])))])]))))

(defn beat-panel []
  (let [beat (re-frame/subscribe [:beat])
        kit (re-frame/subscribe [:kit])
        configs (re-frame/subscribe [:configs])]
    (fn []
      [:div.beat-container
       [:table.seq
        (into ^{:key "beat-tbody"} [:tbody]
          (for [instr (keys @kit)]
            (let [mapping (map-row (get @beat instr) (ticks (get @configs :bars)))]
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
                         "-"]))))))))]])))

(defn slider [key min max step]
  (let [instr-controls (re-frame/subscribe [:instr-controls])
        controls (re-frame/subscribe [:controls])]
    (fn []
      (let [instr (:instr @controls)]
        [:input.track-control
         {:type      "range"
          :min       min
          :max       max
          :step      step
          :value     (get-in @instr-controls [instr key])
          :on-change (fn [e] (re-frame/dispatch [:change-instr-control
                                                 [instr key (js/Number (-> e .-target .-value))]]))}]))))

(defn step-control [key pred f t controls event-key]
  (fn []
    (let [val (get @controls key)]
      [:button.btn
       {:on-click #(when (pred val)
                    (re-frame/dispatch [event-key [key (f val)]]))}
       t])))

(defn select [key values data event-key f]
  (into [:select.form-control.track-control
         {:name      (str key) :value (get @data key)
          :on-change (fn [e] (re-frame/dispatch [event-key
                                                 [key (f (.. e -target -value))]]))}]
        (map (fn [x] [:option {:value x} x]) values)))

(defn step-select [key start end step controls event-key]
  (fn []
    [:div.update-control.form-inline
     [step-control key #(> % start) #(- % step) "-" controls event-key]
     [select key (range start end step) controls event-key js/Number]
     [step-control key #(< % end) #(+ % step) "+" controls event-key]]))

(defn track-control-panel []
  (let [instr-controls (re-frame/subscribe [:instr-controls])
        controls (re-frame/subscribe [:controls])
        editor (re-frame/subscribe [:editor])
        instruments (re-frame/subscribe [:instruments])]
    (fn []
      [:div.track-controls.form
       [step-select :duration 0.25 4.25 0.25 editor :change-editor-control]
       [select :panel ["track" "beat"] controls :change-control identity]
       [select :instr @instruments controls :change-control identity]
       [slider :cutoff 100 5000 50 instr-controls]])))

(defn control-button [name event]
  [:button.form-control.btn-small {:on-click #(re-frame/dispatch [event])} name])

(defn play-control-panel []
  (fn []
    [:div.form-inline
     [control-button "play" :play]
     [control-button "stop" :stop]
     [control-button "sync" :sync-track]]))

(defn main-panel []
  (let [controls (re-frame/subscribe [:controls])]
    (fn []
      [:div.main.row
       [:div.col-md-12
        [:h2 "OverDAW"]]
       [:div.col-md-12.play-controls
        [play-control-panel]]
       [:div
        [:div.col-md-9
         (condp = (:panel @controls)
           "track" [track-panel]
           "beat" [beat-panel])]
        [:div.col-md-3
         [track-control-panel]]]])))
