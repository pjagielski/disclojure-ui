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

(def s
  (->> (phrase [1/2 3/4 3/4 3/4 3/4 1
                3/4 3/4 3/4 3/4 1/2]
               [nil [2 5 9] [2 5 9] [0 4 7] [0 4 7] [0 4 7]
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

(def raw-track
  (->>
    (times 4 s)
    (with (times 4 s1))))

(def track
  (->> raw-track
       (wherever :pitch, :pitch temperament/equal)
       (where :time (bpm 120))
       (where :duration (bpm 120))))

(comment
  (o/volume 1)
  (i/supersaw :freq 100 :duration 2)
  (live/play track)
  (live/jam (var track))
  (live/stop))