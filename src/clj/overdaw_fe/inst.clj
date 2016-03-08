(ns overdaw-fe.inst
  (:require [overtone.live :refer :all]
            [leipzig.live :as live]))

(defcgen cut-out [input {:default :none}]
         (:ar (let [_ (detect-silence input :action FREE)]
                input))
         (:default :ar))

(defcgen effects [input  {:default :none}
                  pan    {:default 0}
                  wet    {:default 0.33}
                  room   {:default 0.5}
                  damp   {:default 0.5}
                  volume {:default 1.0}
                  early  {:default 0.1}
                  high   {:default 20000}
                  low    {:default 0}]
         (:ar (-> input
                  (* volume)
                  (pan2 pan)
                  (free-verb :mix wet :room room :damp damp)
                  (lpf high)
                  cut-out))
         (:default :ar))

(defcgen echo [input  {:default :none}
                max-delay {:default 1.0}]
         (:ar (+ input (comb-n input max-delay 0.4 2.0)))
         (:default :ar))

(defsynth fx-echo-amp [bus 0 max-delay 1.0 delay-time 0.4 decay-time 2.0 amp 0.5]
          (let [source (in bus)
                echo (comb-n source max-delay delay-time decay-time)]
            (replace-out bus (pan2 (+ (* amp echo) source) 0))))

(comment
  (definst talking-bass [freq 130 mod1-partial 2.34 mod1-index 3 mod2-partial 12 mod2-index 30
                         car-partial 0.15 amp 1 dur 1.0]
           (let [mod2 (-> (sin-osc (* freq mod2-partial))
                          (mul-add (* freq mod2-index) 0))
                 lfo (-> (sin-osc 1.25) (mul-add 1 0.9))
                 mod1 (-> (sin-osc (+ mod2 (* freq mod1-partial lfo)))
                          (mul-add (* freq mod1-index) 0))
                 carrier (sin-osc (+ mod1 (* freq car-partial)))]
             (-> carrier
                 (* (env-gen (adsr 0.02 0.2 0.1 0.1) (line:kr 1 0 dur) :action FREE))
                 (* amp)
                 pan2))))

(comment
  (definst d-bass [freq 220 dur 1.0 res 1000 volume 1.0 pan 0 wet 0.33 room 0.5 cutoff 5000 env-amount 0.95]
           (let [fil-env (env-gen (adsr 0.1 0.9 0.1 0.6))]
             (-> (sin-osc [(/ freq 2) (+ (midicps 0.17) (/ freq 2)) (+ (midicps 0.15) (/ freq 2))])
                 ;(tanh)
                 (lpf (+ cutoff (* env-amount fil-env)))
                 (* (env-gen (adsr 0.1 0.4 0.4 0.4) (line:kr 1 0 dur) :action FREE))
                 (* volume)
                 pan2))))

(definst g-bass [freq 110 dur 1.0 res 5000 volume 1.0 pan 0 wet 0.2 room 0.4 cutoff 2000 env-amount 0.55]
   (let [mod (-> (sin-osc (* freq 1.5))
                 (mul-add (* freq 1.5) 50))
         lfo (-> (sin-osc 1) (mul-add 1 0.75))
         fil-env (env-gen (perc 0.3 dur))]
     (-> (sin-osc (+ (* lfo mod) (/ freq 4)))
         (clip2 0.8)
         (rlpf (+ (/ freq 2) (* env-amount cutoff fil-env)) 1/7)
         (* (env-gen (adsr 0.02 0.2 0.1 0.1) (line:kr 1 0 dur) :action FREE))
         (effects :pan pan :wet wet :room room :volume volume))))

(definst supersaw [freq 440 dur 0.2 amp 1 cutoff 10000 env-amount 0.5]
   (let [snd-fn (fn [freq]
                  (let [tune (ranged-rand 2 32)]
                    (+ freq tune)))
         saws (saw (repeatedly 8 #(snd-fn freq)))
         subs (pulse (repeatedly 3 #(snd-fn (/ freq 2))))
         snd  (+ (* 0.3 saws) (* 0.05 subs))
         ;fil-env (env-gen (adsr 0.1 0.9 0.1 0.6) (line:kr 1 0 dur))
         env (env-gen (adsr 0.1 0.25 0.5 0.40) (line:kr 1 0 dur) :action FREE)]
     (-> snd
         (clip2 0.15)
         (rlpf (+ (* freq 2) (env-gen (perc 0.1 dur) :level-scale cutoff)) 0.75)
         (* env amp)
         pan2)))

(definst dub [freq 440 divisor 2.0 depth 2.0 out-bus 0 duration 1 bpm 120 wobble 5/2]
  (let [modulator (/ freq divisor)
        mod-env   (env-gen (lin 1 0 6))
        snd       (lf-tri (+ freq (* mod-env (* freq depth) (sin-osc modulator))))
        trig      (impulse:kr (/ bpm 120))
        swr       (demand trig 0 (dseq [wobble] INF))
        sweep     (lin-exp (lf-tri swr) -1 1 40 8000)
        wob       (lpf snd sweep)
        wob       (* 0.8 (normalizer wob))
        wob       (+ wob (bpf wob 3500 2))
        verb      (* 0.3 (g-verb wob 9 0.7 0.7))
        env       (env-gen (adsr 0.1 1.4 0.20) (line:kr 1 0 duration) :action FREE)]
    (pan2 (* env (+ wob (* 0.8 verb))))))

(definst plucky [freq 440 dur 1 amp 1 cutoff 4500]
   (let [env (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur) :action FREE)
         level (+ freq (env-gen (perc 0 (/ dur 2)) :level-scale cutoff))]
     (-> (pulse freq)
         (lpf level)
         (* env amp)
         (effects :room 0.5 :wet 0.45))))

(comment
  (definst dark-bass [freq 110 volume 2.0 dur 1.0]
           (let [subfreq (/ freq 1)]
             (->
               (sin-osc [subfreq (- subfreq 2) (+ subfreq 2)])
               tanh
               (* (env-gen (perc 0 dur) :action FREE))
               (* (x-line:kr 1 0.6 0.1))
               (* volume)
               pan2))))

(definst indie-bass [freq 440 dur 1.0 amp 1.0 pan 0 wet 0.2 room 0.5 damp 0.7 cutoff 4500 fil-dur 0.5]
   (let [envelope (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur) :action FREE)
         level (+ (* 2 freq) (env-gen (perc 0 fil-dur) :level-scale cutoff))
         osc (mix [(saw freq)
                   (saw (* freq 1.009))
                   (pulse (/ freq 2) 0.5)])]
     (-> osc
         (rlpf level 0.7)
         (* envelope)
         (effects :pan pan :wet wet :room room :volume amp :damp damp))))

(comment
  (definst wide-bass [freq 220 dur 1.0 cutoff 1500 sub-amp 0.4 amp 0.9]
           (let [osc1 (saw freq)
                 osc2 (saw (* freq 0.99))
                 sub (* sub-amp (pulse (/ freq 2)))
                 snd (mix [osc1 osc2 sub])
                 snd (lpf snd (+ 100 (env-gen (perc 0 1.5) :level-scale cutoff)))
                 env (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur) :action FREE)]
             (pan2 (* snd env amp)))))

(definst bass [freq 220 dur 1.0 amp 0.5 osc-mix 0.7 cutoff 4500]
   (let [sub-freq (/ freq 2)
         osc1 (saw:ar freq)
         osc2 (pulse sub-freq 0.51)
         osc (+ (* osc-mix osc2) (* (- 1 osc-mix) osc1))
         snd [osc osc]
         level (+ (/ freq 2) (env-gen (perc 0 dur) :level-scale cutoff))
         snd (rlpf snd level 0.6)
         env (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur) :action FREE)]
     (out 0 (* amp env snd))))

(comment
  (definst bend-noise [amp 0.7 decay 0.85 cutoff 0.65]
           (let [osc (white-noise)
                 snd [osc osc]
                 snd (bpf snd (lin-exp cutoff 0.0 1.0 20.0 20000.0) 0.5)
                 env (env-gen (env-adsr 0.0 decay 0.0 0.7) :action FREE)]
             (out 0 (* amp env snd)))))

(definst pad [freq 220 dur 1.0 amp 0.5 pan 0 cutoff 2500 fil-amt 100]
   (let [env (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur) :action FREE)
         fil-env (+ fil-amt (env-gen (perc 0 dur) :level-scale cutoff))
         osc (sin-osc freq)]
     (-> osc
         (lpf fil-env)
         (* env amp)
         (echo)
         pan2)))

(definst strings [freq 220 dur 1.0 amp 1 cutoff 0.1 contour 0.3]
   (let [freqs [freq (* 0.99 freq) (* 1.10 freq)]
         src   (saw freqs)
         fil-env (env-gen (adsr 0.1 0.9 0.1 0.6))
         snd   (bpf src (+ (* fil-env (* contour freq 50))
                           (lin-exp cutoff 0.0 1.0 20.0 20000.0)))
         env   (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur) :action FREE)]
     (pan2 (* amp env snd))))

(comment
  (definst growl [freq 220 amp 1 attack 1 dur 1 release 1]
           (let [e (env-gen (perc :attack attack :sustain dur :release release) :action FREE)
                 src (lpf (mix [(saw (* 0.25 freq))
                                (sin-osc (* 1.01 freq))]))
                 ;src (pitch-shift src 0.4 1 0 0.01)
                 src (pan2:ar (* amp e src))]
             src)))

(def atw-bass (sample "resources/sounds/samples/atw_bass.aif"))
(def robot-drive (sample "resources/sounds/samples/robot_drive.wav"))
(def the-one (sample "resources/sounds/samples/the_one.wav"))

(defsynth sampler [in 0 bpm 128 beats 4 amp 1 pos 0 out-bus 0]
    (let [rate (/ (buf-dur in) (* beats (/ 60 bpm)))
          output (play-buf 1 in rate :start-pos pos :action FREE)]
      (out out-bus (* amp (pan2 output)))))

(def samples
  {:atw-bass #(sampler atw-bass :beats 16)
   :the-one #(sampler the-one :beats 8)
   :robot-drive #(sampler robot-drive :beats 2)})

(defmethod live/play-note :sampler [note]
  (when-let [f (-> note :sample samples)]
    (f)))

(comment
  (stop)
  (plucky)
  (dark-ambience)
  (growl :freq 660)
  (atw-bass)
  (the-one)
  (bend-noise)
  (pad)
  (talking-bass :mod1-partial 0.5 :mod1-index 20)
  (strings :freq 110)
  (bass :freq 110)
  (wide-bass :freq 110)
  (indie-bass :freq 110 :room 0.75 :damp 0.2 :dur 0.5)
  (supersaw :freq 660 :cutoff 5000 :env-amount 0.9)
  (g-bass :freq 220 :cutoff 5000 :env-amount 0.1)
  (d-bass)
  (dark-bass 440)
  (dub 200))
