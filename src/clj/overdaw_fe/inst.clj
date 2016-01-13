(ns overdaw-fe.inst
  (:require [overtone.live :refer :all]))

(definst supersaw [freq 440 duration 0.2 amp 1.75 cutoff 0.85 contour 0.45]
   (let [snd-fn (fn [freq osc]
                  (let [tune (ranged-rand 2 32)]
                    (osc (+ freq tune))))
         saws (mix (repeatedly 13 #(snd-fn freq saw)))
         subs (mix (repeatedly 5 #(snd-fn (/ freq 4) pulse)))
         snd  (+ (* 1.1 saws) (* 0.45 subs))
         fil-env (env-gen (adsr 0.1 0.9 0.1 0.6))
         snd (rlpf snd (+ (* fil-env (* contour 10000))
                          (lin-exp cutoff 0.0 1.0 20.0 20000.0)) 0.65)
         env (env-gen (adsr 0.1 0.45 0.20) (line:kr 1 0 duration) :action FREE)]
     (pan2 (* snd env amp))))
