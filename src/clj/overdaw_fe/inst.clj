(ns overdaw-fe.inst
  (:require [overtone.live :refer :all]))

(defcgen cut-out [input {:default :none}]
         (:ar (let [_ (detect-silence input :action FREE)]
                input))
         (:default :ar))

(defcgen effects [input  {:default :none}
                  pan    {:default 0}
                  wet    {:default 0.33}
                  room   {:default 0.5}
                  volume {:default 1.0}
                  early  {:default 0.1}
                  high   {:default 20000}
                  low    {:default 0}]
         (:ar (-> input
                  (* volume)
                  (pan2 pan)
                  (free-verb :mix wet :room room)
                  (lpf high)
                  cut-out))
         (:default :ar))

(definst bass [freq 110 dur 1.0 res 1000 volume 1.0 pan 0 wet 0.5 room 0.5]
  (-> (sin-osc freq)
      (+ (* 1/3 (sin-osc (* 2 freq))))
      (+ (* 1/2 (sin-osc (* 3 freq))))
      (+ (* 1/3 (sin-osc (* 5 freq))))
      (clip2 0.8)
      (rlpf res 1/7)
      (* (env-gen (adsr 0.02 0.2 0.1 0.1) (line:kr 1 0 dur)))
      (effects :pan pan :wet wet :room room :volume volume)))

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

(defsynth dub [freq 440 divisor 2.0 depth 2.0 out-bus 0 duration 1 bpm 120 wobble 5/2]
  (let [modulator (/ freq divisor)
        mod-env   (env-gen (lin 1 0 6))
        snd       (lf-tri (+ freq (* mod-env (* freq depth) (sin-osc modulator))))
        ;snd       (sin-osc freq)
        trig      (impulse:kr (/ bpm 120))
        swr       (demand trig 0 (dseq [wobble] INF))
        sweep     (lin-exp (lf-tri swr) -1 1 40 8000)
        wob       (lpf snd sweep)
        wob       (* 0.8 (normalizer wob))
        wob       (+ wob (bpf wob 3500 2))
        verb      (* 0.3 (g-verb wob 9 0.7 0.7))
        env       (env-gen (adsr 0.1 1.4 0.20) (line:kr 1 0 duration) :action FREE)]
    (out out-bus (pan2 (* env (+ wob (* 0.8 verb)))))))

(comment
  (stop)
  (demo 3 (dub 100))
  (demo 3 (mix (saw [99 100 101])))
  (demo 3 (wobble (mix (saw [99 100 101])) 3))
  (demo 5 (wobble (mix (saw [99 100 101])) 0.3)))
