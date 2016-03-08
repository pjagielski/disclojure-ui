(ns overdaw-fe.track
  (:require [overtone.live :as o]
            [leipzig.melody :refer :all]
            [leipzig.chord :refer :all]
            [leipzig.scale :as scale]
            [leipzig.live :as live]
            [leipzig.temperament :as temperament]
            [overdaw-fe.inst :as i]
            [overdaw-fe.kit :as k]
            [overdaw-fe.play]))

(def kit (atom (k/make-kit "big_room")))

(defn tap [drum times length & {:keys [amp] :or {amp 1}}]
  (map #(zipmap [:time :duration :drum :amp]
                [%1 (- length %1) drum amp]) times))

(defmethod live/play-note :beat [note]
  (when-let [fn (-> (get @kit (:drum note)) :sound)]
    (fn :amp (:amp note))))

(def s
  (->> (phrase [1/2 3/4 3/4 3/4 3/4 1
                3/4 3/4 3/4 3/4 1/2]
               [nil [3 5 9] [2 5 9] [0 4 7] [0 4 7] [0 4 7]
                [-1 2 5] [-1 2 5] [-1 2 5] [-1 2 5] [0 5 7]])
       (wherever :pitch, :pitch (scale/from (o/note :C5)))
       (all :amp 2)
       (all :part :supersaw)))

(def s1
  (->> (phrase [1/2 3/4 3/4 3/4 3/4 1
                3/4 3/4 3/4 3/4 1/2]
               [nil 2 5 4 2 0
                2 5 4 0 -3])
       (wherever :pitch, :pitch (scale/from (o/note :C6)))
       (all :part :supersaw)))

(def s2
  (->> (phrase [4 4]
               [(-> triad (root 1))
                (-> triad (inversion 2) (root 4))])
       (wherever :pitch, :pitch (comp scale/C scale/major))
       (where :amp (is 1))
       (all :part :indie-bass)))

(def b1
  (->> (phrase [3 3 2]
               [[0 7] 13 4])
       (wherever :pitch, :pitch (scale/from (o/note :C2)))
       (where :part (is :bass))))

(def b2
  (->> (phrase [3/4 1/4 1/2 1/4 1/2 1/4 1/2 3/4 1/4 1/2 1/4 3/4 1/4 1/4 1/4 1/2 1/4 1/2 1/4 1/4]
               [-2 nil -2 nil -2 nil -2 -2 nil -1 nil -1 0 -3 nil -3 -3 -3 -4 -3])
       (wherever :pitch, :pitch (scale/from (o/note :C3)))
       (all :part :garage-bass)
       (all :amp 2.0)))

(def b3
  (->> (phrase [1/4 1/4 1/4 1 1/2 1/2 1 1/4]
               [10 10 10 nil 10 10 nil 10])
       (wherever :pitch, :pitch (scale/from (o/note :C2)))
       (all :part :indie-bass)
       (all :amp 2.0)))

(def b4
  (->> (phrase (repeat 4)
               [0 0 4 4])
       (wherever :pitch, :pitch (scale/from (o/note :C3)))
       (where :part (is :wide-bass))
       (all :amp 2.0)))

(def b5
  (->> (phrase (cycle [(- 4 1/2) (+ 4 1/2)])
               [8 6 4 1])
       (wherever :pitch, :pitch (scale/from (o/note :C2)))
       (all :part :wide-bass)
       (all :amp 2.0)))

(def b6
  (let [theme [3/4 3/4 3/4 1/2 3/4 1/2]]
    (->> (phrase (concat [3/4 3/4 3/4 1/2 1/4 1/2 1/2]
                         (flatten (repeat 3 theme)))
                 (concat
                         [4 4 4 4 16 4 16]
                         ;[4 4 4 4 4 4 4]
                         [7 7 7 7 7 7]
                         [9 9 9 9 9 9]
                         [11 11 12 12 12 12]))
         (wherever :pitch, :pitch (scale/from (o/note :C2)))
         (all :part :bass)
         ;(where :part (is :indie-bass))
         (all :amp 2.0))))

(def random-bass
  (->> (phrase (repeat 1/2)
               (take 16 (repeatedly #(int (rand 7))))
               ;(concat (range 8) (range 8 0 -1))
               )
       (wherever :pitch, :pitch (comp scale/C scale/low scale/pentatonic))
       ;(where :part (is :g-bass))
       (all :part :pad)
       ;(where :part (is :supersaw))
       ;(all :amp 2.0)
        ))

(def pretend-bass
  (->> (phrase (repeat 1/2)
               (concat
                 (repeat 7 [[9 2 -10]])
                 [[9 -3]]
                 (repeat 6 [[2 -5 -17]])
                 [[2 -10]]
                 [[9 -3]]))
       (wherever :pitch, :pitch (scale/from (o/note :C4)))
       (all :part :indie-bass)))

(def face-bass
  (let [theme (concat (repeat 5 3/4) [1/4])]
    (->> (phrase (flatten (repeat theme))
                 (concat (repeat 6 -4)
                         (repeat 6 -6)
                         (repeat 6 -8)))
         (then (phrase (concat (repeat 4 3/4) [1])
                       [-11 -11 -11 -6 -6]))
         (wherever :pitch, :pitch (scale/from (o/note :C3)))
         (all :part :bass))))

(def face-glide
  (->> (phrase [3 1 4 3 1 2 1 1]
               [-4 -4 -6 -8 -8 -11 -6 -6])
       (wherever :pitch, :pitch (scale/from (o/note :C3)))
       (where :amp (is 1))
       (all :part :bass)))

(def steel-drum
  (->> (phrase [3/4 3/4 1 1/2 1 3/4 3/4 1/2 1/4 1/4 1/2 1]
               [-3 -3 -3 -3 -3 0 0 0 nil 7 4 4])
       (wherever :pitch, :pitch (scale/from (o/note :C3)))
       (where :amp (is 1))
       (all :part :bass)))

(def steel-drum-2
  (->> steel-drum
       (all :part :indie-bass)))

(def steel-drum-3
  (->> (phrase [3/4 3/4 1 1/2 1 3/4 3/4 1/2 1/4 1/4 1/2 1]
               [-3 -3 -3 -3 -3 0 0 0 nil 7 4 4])
       (wherever :pitch, :pitch (scale/from (o/note :C5)))
       (all :part :supersaw)))

(def steel-drum-4
  (->> (phrase [3/4 3/4 1 1/2 1 3/4 3/4 1/2 1/4 1/4 1/2 1]
               [-3 -3 -3 -3 -3 0 0 0 nil 7 4 4])
       (wherever :pitch, :pitch (scale/from (o/note :C4)))
       (all :part :strings)))

(def phantoms
  (->> (phrase (concat (take 20 (repeat 3/4)) [1/2 1/4])
               (concat [5 12 3 10]
                       (take 16 (cycle [0 5 0 8]))
                       [0 3]))
       (wherever :pitch, :pitch (scale/from (o/note :C5)))
       (all :amp 0.4)
       (all :part :supersaw)))

(def the-one
  (->> (phrase [1/2 1/2 1/2 1/4 1/2 1/4 1/2 1/4 1/2 1/4 1/2 1/2 1/2 1/2 1/2 1/4 1/2 1/4 1/2]
               [-1 -1 -1 nil 3 nil 3 nil 3 nil 3 nil 3 nil 5 nil 5 nil 5])
       (wherever :pitch, :pitch (scale/from (o/note :C2)))
       (all :amp 1)
       (all :part :plucky)))

(def house-beat-1
  (->>
    (reduce with
      [
       (tap :kick (range 8) 8)
       ;(tap :snare1 [(+ 4 7/4)] 8 :amp 1.5)
       ;(tap :snare2 [9/4] 8 :amp 0.7)
       ;(tap :claps (range 1 8 2) 8)
       ;(tap :hat_1 (range 1/2 8 1) 8)
       ;(tap :hat_1 [7/4 9/4 23/4 25/4] 8 :amp 0.2)
       ;(tap :hat_2 (range 1/2 8 1) 8)
       ;(tap :hat_2 [1/4 (+ 4 1/4)] 8 :amp 0.4)
       ;(tap :hat_3 (range 1/2 8 1) 8)
       ;(tap :hat_4 [0 2 5/2 3 7/2 4 6 13/2 7 15/2] 8)
       ])
    (all :part :beat)))

(def garage-beat-1
  (->>
    (reduce with
      [(tap (keyword "808 Kick") [0] 8)
       (tap :kick [0 11/4 14/4 6 27/4 30/4] 8)
       (tap :snare [1 3 5 7] 8)
       (tap :hat (range 1/2 8) 8)
       (tap :shaker [1/4 6/4 7/4 9/4 17/4 18/4 22/4 25/4 26/4 27/4] 8 :amp 0.3)
       (tap :tambourine (range 0 8 1/2) 8 :amp 1)
       (tap :tambourine (range 1/4 8 1/2) 8 :amp 0.4)
       (tap :technologic [0] 8)
       ])
    (all :part :beat)))

(def sampler
  (->> (concat [
                ;{:time 0 :sample :atw-bass}
                ;{:time 4 :sample :robot-drive}
                {:time 0 :sample :the-one}
                ])
       (all :part :sampler)
       (all :duration 0)))

(def initial-track {
                    :beat   (times 2 house-beat-1)
                    ;:plucky    the-one
                    ;:supersaw (->> (times 2 s1)
                    ;               (with (times 2 s)))
                    ;:supersaw (->> phantoms (all :part :supersaw))
                    ;:plucky (->> phantoms
                    ;             (all :part :plucky)
                    ;             (with (times 2 the-one))
                    ;             )
                    ;:sampler  sampler
                    })

(defonce raw-track (ref initial-track))

(defn track [raw-track]
  (->> raw-track
       vals
       (reduce with)
       (wherever :pitch, :pitch temperament/equal)
       (where :time (bpm 128))
       (where :duration (bpm 128))))

(comment
  (o/volume 1)
  (dosync (ref-set raw-track initial-track))
  (apply (-> kit deref :kick :sound) [:amp 0.5])
  (live/play (track initial-track))
  (live/stop))
