(ns overdaw-fe.track
  (:require [overtone.live :as o]
            [leipzig.melody :refer :all]
            [leipzig.chord :refer :all]
            [leipzig.scale :as scale]
            [leipzig.live :as live]
            [disclojure.kit :as k]
            [disclojure.sampler :as s]
            [overdaw-fe.play]
            [clojure.java.io :as io]))

;(k/load-kit! (io/resource "sounds/kits/big_room"))
;(k/load-kit! (io/resource "sounds/kits/2step"))
(k/load-kit! (io/file "work/beats/da-funk"))
(s/load-samples! (io/file "work/samples"))

(defn tap [drum times length & {:keys [amp] :or {amp 1}}]
  (map #(zipmap [:time :duration :drum :amp]
                [%1 (- length %1) drum amp]) times))

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
               (take 16 (repeatedly #(int (rand 7)))))
               ;(concat (range 8) (range 8 0 -1))

       (wherever :pitch, :pitch (comp scale/C scale/low scale/pentatonic))
       ;(where :part (is :g-bass))
       (all :part :pad)))
       ;(where :part (is :supersaw))
       ;(all :amp 2.0)

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
       (all :amp 2)
       (all :part :plucky)))

(def chords-1
  (->> (phrase [2 3/2 9/2]
               (cycle [[0 4 -3 -5] [-1 2 -5 -7] [0 -3 -6 -8]]))
       (wherever :pitch, :pitch (scale/from (o/note :C4)))
       (all :amp 1)
       (all :part :strings)))

(def show-love
  (->> (phrase [1/4 3/4 1/4 1/4 1/4 1/4 1/2 1/4 1/2 1/4 1/2 1/4 3/4 1/4 1/2 1/4 1/2 1/4 1/2 1/4 1/4 1/4]
               [[5 -2] nil 5 nil 7 [2 -5] nil 2 nil 2 nil [3 -4] nil 3 nil 3 nil 3 nil 3 [4 -3] nil])
       (wherever :pitch, :pitch (comp scale/low scale/C scale/major))
       (all :amp 2)
       (all :part :plucky)))

(def show-love-theme (times 2 show-love))

(def warp
  (let [len 1/2]
    (->> (phrase (concat [1] (cycle [len (- 4 len)]))
                 (cycle [nil 0]))
         (take 4)
         (wherever :pitch :pitch (comp scale/C scale/major))
         (all :part :warp-bass)
         (all :amp 1))))

(def leanon-chords
  [[-9 -2 0 2 4]
   [-8 -1 -3 1 3]
   [-7 0 2 4]
   [-5 -1 2 4 5 6]
   [-5 -1 2 3 4]])

(def leanon
  (let [[ch1 ch2 ch3 ch4 ch5] leanon-chords]
    (->> (phrase (concat (take 9 (cycle [1/2 1/4]))
                         [1/2]
                         (take 9 (cycle [1/2 1/4]))
                         [1/2])
                 [ch1 nil ch1 nil ch1 nil ch2 nil ch2 nil ch3 nil ch3 nil ch3 nil ch4 nil ch4 ch5])
         (wherever :pitch, :pitch (comp scale/low scale/G scale/minor))
         (all :part :plucky)
         (all :amp 1))))

(def count-on-me
  (let [[ch1 ch2 ch3 ch4] [[4 6 8] [5 7 9] [6 8 10] [7 9 11]]]
    (->> (phrase [1/2 1/2 1/2 1/4 1/2 1/4 1/2 1/2 1/2 1/2 1/2 1/2 1/4 1/2 1/4 1/2 1/2 1/2]
                 [ch1 nil ch1 nil ch2 nil ch2 nil ch2 ch2 nil ch2 nil ch3 nil ch3 ch4 ch4])
         (wherever :pitch, :pitch (comp scale/low scale/G scale/minor))
         (all :part :piano)
         (all :amp 1))))

(def kamikaze
  (let [[ch1 ch2 ch3 ch4 ch5] [[-3 0] [-5 -1 2] [-5 -2] [-6 -3] [-4 -1]]]
    (->> (phrase [1/2 1/4 1/2 1/4 1/2 1/2 1/4 1/2 1/4 1/2 1/2 1/4 1/2 1/4 1/2 1/4 1/2 1/4 1/2 1/2]
                 [ch1 nil ch1 nil ch2 ch3 nil ch4 nil ch5 ch1 nil ch1 nil ch2 nil ch3 nil ch4 nil])
         (wherever :pitch, :pitch (comp scale/E scale/major))
         (all :part :plucky)
         (all :amp 1))))

(def da-funk
  (->> (phrase [2 1/2 1/2 1/2 2.5 1/2 1/2 1/2 2.5 1/2 1/2 1/2 2.5 1 1]
               [0 -1 0 2 -3 -4 -3 -1 -5 -6 -5 -3 -7 -6 -5])
       (where :pitch (comp scale/G scale/minor))
       (all :part :da-funk)
       (all :amp 1)))

(def you-and-me-phrase
  (->> (phrase (concat (take 11 (cycle [1/2 1/4 1/2 3/4]))
                       [1/2 1/2 7/4])
               [-4 nil -4 nil -1 nil -1 nil 0 nil 0 0 0 nil])
       (wherever :pitch, :pitch (comp scale/low scale/low
                                      scale/A scale/sharp scale/minor))
       (all :part :plucky)
       (all :amp 1)))

(def you-and-me
  (->> you-and-me-phrase
       (then (->> you-and-me-phrase
                  (wherever :pitch, :pitch scale/raise)
                  (all :amp 0.5)))))

(def levels
  (->> (phrase (repeat 1/2)
               [[0 4 10] [0 4 9] [0 4 9] nil [2 6 9] [2 6 9] [2 6 9] [2 6 9] [-1 3 8] [-1 3 8] [-1 3 9] [-2 2 9] nil [2 9 14] [2 9 13] [2 9 11]
                [0 4 10] [0 4 9] [0 4 9] nil [2 6 9] [2 6 9] [2 6 9] [2 6 9] [-1 3 7] [-1 3 7] [-1 3 6] [-2 2 6] nil [2 9 14] [2 9 13] [2 9 11]])
       (wherever :pitch, :pitch (comp scale/C scale/sharp scale/minor))
       (all :part :supersaw)
       (all :amp 1)))

(def one
  (->> (phrase (concat (take 9 (repeat 3/4)) [1/4 1/2 1/2])
               [5 4 3 2 3 3 3 3 3 nil 3 3])
       (wherever :pitch, :pitch (comp scale/C scale/high scale/major))
       (all :part :supersaw)
       (all :amp 1)))

(def miami
  (letfn [(make-chord [root] [root (- root 24) (+ root 12) (+ root 24)])]
    (let [[ch1 ch2 ch3 ch4 ch5 ch6] (map make-chord [75 80 83 82 85 87])]
      (->> (phrase (cycle (concat (take 10 (repeat 3/4)) [1/2]))
                   [ch1 ch1 ch2 ch2 ch2 ch2 ch2 ch2 ch3 ch3 ch4
                    ch1 ch1 ch2 ch2 ch2 ch2 ch2 ch2 ch3 ch5 ch6])
           ;(wherever :pitch, :pitch (comp scale/G scale/high scale/minor))
           (all :part :supersaw)
           (all :amp 1)))))

(def da-beats
  (->>
    (reduce with
      [
       (tap :fat-kick (range 8) 8)
       (tap :kick (range 8) 8)
       (tap :snare (range 1 8 2) 8)
       (tap :close-hat (sort (concat [3.75 7.75] (range 1/2 8 1))) 8)
       (let [horns [0 1/2 5/4 2]]
         (tap :horn (concat horns
                            (map (partial + 4) horns)) 8))
       ])
    (all :part :beat)))

(def house-beat
  (->>
    (reduce with
      [
       (tap :kick_2 (range 8) 8)
       (tap :big_kick (range 8) 8)
       ;(tap :snare1 (range 8) 8)
       ;(tap :claps (range 8) 8)
       ;(tap :hat_1 (range 8) 8)
       ;(tap :snare1 [(+ 4 7/4)] 8 :amp 1.5)
       ;(tap :snare2 [9/4] 8 :amp 0.7)
       (tap :snap (range 1 8 2) 8)
       (tap :claps (range 1 8 2) 8)
       (tap :hat_1 (range 1/2 8 1) 8)
       ;(tap :hat_1 [7/4 9/4 23/4 25/4] 8 :amp 0.2)
       ;(tap :hat_2 (range 1/2 8 1) 8)
       ;(tap :hat_2 [1/4 (+ 4 1/4)] 8 :amp 0.4)
       ;(tap :hat_3 (range 1/2 8 1) 8)
       ;(tap :hat_4 [0 2 5/2 3 7/2 4 6 13/2 7 15/2] 8)
       ])
    (all :part :beat)))

(def lean-beat
  (->>
    (reduce with
        [
         ;(tap :kick [0 5/2 4 (+ 3/4 5) 13/2] 8 :amp 1.5)
         (tap :kick_3 [0 5/2 4 (+ 3/4 5) 13/2] 8 :amp 1.5)
         (tap :kick_2 [0 5/2 4 (+ 3/4 5) 13/2] 8 :amp 1.5)
         ;(tap :big_kick [0 5/2 4 (+ 3/4 5) 13/2] 8 :amp 1.5)
         (tap :snap (range 1 8 2) 8)
         (tap :snare1 (range 1 8 2) 8)
         (tap :snare2 (range 1 8 2) 8)
         (tap :hat_4 (range 1 8 4) 8)
         ])
    (all :part :beat)))

(def lean-kick
  (->>
    (reduce with
            [(tap :kick_2 (range 8) 8)
             (tap :kick_3 (range 8) 8)])
    (all :part :beat)))

(def lean-hat
  (->>
    (let [hat-seq [0 0.75 1.5 2 2.75 3.25 3.5 3.75]]
      (reduce with
              [(tap :kick_2 [0] 8)
               (tap :snare4 (concat hat-seq
                                    (map (partial + 4) hat-seq)) 8)]))
    (all :part :beat)))

(def garage-beat
  (->>
    (reduce with
      [
       (tap (keyword "808 Kick") [0] 8)
       (tap :kick [0 11/4 14/4 6 27/4 30/4] 8)
       (tap :snare [1 3 5 7] 8)
       ;(tap :hat (range 1/2 8) 8 :amp 0.5)
       ;(tap :shaker [1/4 6/4 7/4 9/4 17/4 18/4 22/4 25/4 26/4 27/4] 8 :amp 0.3)
       ;(tap :tambourine (range 0 8 1/2) 8 :amp 1)
       ;(tap :tambourine (range 1/4 8 1/2) 8 :amp 0.4)
       ])
    (all :part :beat)))

(def metro (atom 110))

(def initial-track {
                    :bass face-bass
                    ;:supersaw miami
                    ;:beat (times 2 lean-beat)
                    ;:beat    (times 2 lean-kick)
                    ;:beat    (times 4 lean-hat)
                    ;:beat   (times 2 garage-beat)
                    ;:beat    (times 2 da-beats)
                    ;:da-funk (da-funk)
                    ;:tb303 da-funk-303
                    ;:plucky  kamikaze
                    ;:plucky  you-and-me
                    ;:plucky  (times 2 leanon-1)
                    ;:beat   (times 2 house-beat)
                    ;:beat   (times 2 garage-beat)
                    ;:bass  b6
                    ;:plucky (times 2 the-one)
                    ;:supersaw (->> (times 2 s1)
                    ;               (with (times 2 s)))
                    ;:supersaw (->> phantoms (all :part :supersaw))
                    ;:plucky (->> show-love-theme
                    ;             (with (->> show-love-theme
                    ;                        (wherever :pitch :pitch scale/raise)))
                    ;             )
                    ;:plucky (->> phantoms
                    ;             (all :part :plucky)
                    ; (with (times 2 the-one))
                    ;             )
                    ;:strings (times 2 chords-1)
                    ;:sampler (sampler [[0 :lean-intro 1]
                    ;                   [8 :lean-intro 1]])
                    })


(comment
  (o/volume 1)
  (live/play-note {:part :da-funk :pitch 233.08188075904496 :duration 1})
  (live/play-note {:part :sampler :sample :da-funk-303 :bpm 110})
  (live/play-note {:part :sampler :sample :you-and-me-chorus :bpm 130})
  (live/play-note {:part :sampler :sample :lean-chorus :bpm 100})
  (live/play-note {:part :sampler :sample :smack-beat :bpm 126})
  (live/play-note {:part :sampler :sample :lean-chorus :beats 4 :bpm 100})
  (live/play-note {:part :sampler :sample :lean-intro :bpm 100})
  (live/play (track initial-track))
  (live/stop))
