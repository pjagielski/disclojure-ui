(ns overdaw-fe.track
  (:require [overtone.live :as o]
            [leipzig.melody :refer :all]
            [leipzig.scale :as scale]
            [leipzig.live :as live]
            [leipzig.temperament :as temperament]
            [overdaw-fe.inst :as i]
            [overdaw-fe.kit :as k]
            [overdaw-fe.play]))

(def kit (k/make-kit "2step"))

(defn tap [drum times length & {:keys [amp] :or {amp 1}}]
  (map #(zipmap [:time :duration :drum :amp]
                [%1 (- length %1) drum amp]) times))

;; todo kit global variable
(defmethod live/play-note :beat [note]
  (when-let [fn (-> note :drum kit :sound)]
    (fn :amp (:amp note))))

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
  (->> (phrase [3 3 2]
               [[0 7] 13 4])
       (wherever :pitch, :pitch (scale/from (o/note :C2)))
       (where :part (is :bass))))

(def b2
  (->> (phrase [3/4 1/4 1/2 1/4 1/2 1/4 1/2 3/4 1/4 1/2 1/4 3/4 1/4 1/4 1/4 1/2 1/4 1/2 1/4 1/4]
               [-2 nil -2 nil -2 nil -2 -2 nil -1 nil -1 0 -3 nil -3 -3 -3 -4 -3])
       (wherever :pitch, :pitch (scale/from (o/note :C3)))
       (where :part (is :bass))
       (all :amp 2.0)))

(def b3
  (->> (phrase [1/4 1/4 1/4 1 1/2 1/2 1 1/4]
               [10 10 10 nil 10 10 nil 10])
       (wherever :pitch, :pitch (scale/from (o/note :C2)))
       (where :part (is :indie-bass))
       (all :amp 2.0)))


(def b4
  (->> (phrase (repeat 4)
               [5 7 8 8])
       (wherever :pitch, :pitch (scale/from (o/note :C3)))
       (where :part (is :wide-bass))
       (all :amp 2.0)))

(def b5
  (->> (phrase (cycle [(- 4 1/2) (+ 4 1/2)])
               [8 6 4 1])
       (wherever :pitch, :pitch (scale/from (o/note :C2)))
       (where :part (is :wide-bass))
       (all :amp 2.0)))

(def beat-1
  (->>
    (reduce with
            [(tap :snare1 [2 6] 8)
             (tap :snare2 [2 6] 8)
             (tap :hat (range 3 8 4) 8)
             (tap :kick  [0 4 7] 8)])
    (all :part :beat)))

(def beat-0
  (->>
    (reduce with [(tap :kick [0] 8)])
    (all :part :beat)))

(def house-beat-1
  (->>
    (reduce with
      [(tap :kick (range 8) 8)
       (tap :snare1 [7/4 (+ 4 7/4)] 8 :amp 1.5)
       (tap :snare2 [9/4 (+ 4 9/4)] 8 :amp 0.7)
       (tap :claps   (range 1 8 2) 8)
       (tap :hat_1 (range 1/2 8 1) 8)
       (tap :hat_1 [7/4 9/4 23/4 25/4] 8 :amp 0.5)
       (tap :hat_2 (range 1/2 8 1) 8)
       (tap :hat_2 [1/4 (+ 4 1/4)] 8 :amp 0.4)
       (tap :hat_3 (range 1/2 8 1) 8)
       (tap :hat_4 [0 2 5/2 3 7/2 4 6 13/2 7 15/2] 8)
       ])
    (all :part :beat)))

(def garage-beat-1
  (->>
    (reduce with
      [(tap (keyword "808 Kick") [0] 8)
       (tap :kick [0 11/4 14/4 6 27/4 30/4] 8)
       (tap :snare [1 3 5 7] 8)
       (tap :hat (range 1/2 8) 8)
       ;(tap :shaker [1/4 6/4 7/4 9/4 17/4 18/4 22/4 25/4 26/4 27/4] 8 :amp 0.3)
       ;(tap :tambourine (range 0 8 1/2) 8 :amp 1)
       ;(tap :tambourine (range 1/4 8 1/2) 8 :amp 0.4)
       ;(tap :technologic [0] 8)
       ])
    (all :part :beat)))

(def raw-track
  (->>
    ;(times 1 house-beat-1)
    (times 2 garage-beat-1)
    ;(times 1 beat-0)
    ;(with (times 1 b2))
    (with (times 1 b4))
    ;(with (times 1 b5))
    ;(with (times 2 s))
    ;(with (times 2 s1))
    ))

(defn track [raw-track]
  (->> raw-track
       (wherever :pitch, :pitch temperament/equal)
       (where :time (bpm 128))
       (where :duration (bpm 128))))

(comment
  (o/volume 1)
  (i/d-bass :freq 220 :dur 0.5)
  (i/g-bass :freq 100 :dur 1 :volume 2)
  (i/wide-bass :freq 100 :dur 1 :volume 2)
  (i/dub :freq 200 :bpm 120 :wobble 2)
  (i/talking-bass :freq 130 :amp 3 :dur 3)
  (apply (-> :hat kit :sound) [:amp 0.5])
  (apply (-> :kick kit :sound) [:amp 0.5])
  (i/supersaw :freq 440 :duration 0.5)
  (live/play (track raw-track))
  (live/stop))
