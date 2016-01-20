(ns overdaw-fe.track
  (:require [overtone.live :as o]
            [leipzig.melody :refer :all]
            [leipzig.scale :as scale]
            [leipzig.live :as live]
            [leipzig.temperament :as temperament]
            [overdaw-fe.inst :as i]))

(defmethod live/play-note :supersaw [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/supersaw :freq hertz :duration seconds :amp (or amp 1))))

(defmethod live/play-note :bass [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/bass :freq hertz :dur seconds :volume (or amp 1))))

(def kit
  {:kick (o/sample "resources/sounds/kick.wav")
   :snare (o/sample "resources/sounds/snare.wav")
   :hat (o/sample "resources/sounds/shaker.wav")})

(defn tap [drum times length]
  (map #(zipmap [:time :duration :drum] [%1 (- length %1) drum]) times))

(defmethod live/play-note :beat [note] ((-> note :drum kit) :amp 1.2))

(def s
  (->> (phrase [1/2 3/4 3/4 3/4 3/4 1
                3/4 3/4 3/4 3/4 1/2]
               [nil [3 5 9] [2 5 9] [0 4 7] [0 4 7] [0 4 7]
                [-1 2 5] [-1 2 5] [-1 2 5] [-1 2 5] [0 5 7]])
       (wherever :pitch, :pitch (scale/from (o/note :C5)))
       (where :part (is :supersaw))))

(def s1
  (->> (phrase [1/2 3/4 3/4 3/4 3/4 1
                3/4 3/4 3/4 3/4 1/2]
               [nil 2 5 4 2 0
                2 5 4 0 -3])
       (wherever :pitch, :pitch (scale/from (o/note :C6)))
       (where :part (is :supersaw))))

(def b1
  (->> (phrase [1/2 3/4 3/4 3/4 3/4 1
                3/4 3/4 3/4 3/4 1/2]
               [nil 2 5 4 2 0 2 5 4 0 -3])
       (wherever :pitch, :pitch (scale/from (o/note :C2)))
       (where :part (is :bass))))

(def beat-1
  (->>
    (reduce with
            [(tap :snare [1 3 5 7] 8)
             (tap :kick  [0 1 2 3 15/4 4 5 6 7] 8)])
    (all :part :beat)))

(def raw-track
  (->>
    (times 2 s)
    (with (times 2 s1))
    (with (times 2 beat-1))))

(def track
  (->> raw-track
       (wherever :pitch, :pitch temperament/equal)
       (where :time (bpm 120))
       (where :duration (bpm 120))))

(comment
  (o/volume 1)
  (i/bass)
  (i/supersaw :freq 440 :duration 0.5)
  (live/play track)
  (live/jam (var track))
  (live/stop))