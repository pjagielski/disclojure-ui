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

(defcgen echo [input  {:default :none}
                max-delay {:default 1.0}]
         (:ar (+ input (comb-n input max-delay 0.4 2.0)))
         (:default :ar))

(defsynth fx-echo-amp [bus 0 max-delay 1.0 delay-time 0.4 decay-time 2.0 amp 0.5]
          (let [source (in bus)
                echo (comb-n source max-delay delay-time decay-time)]
            (replace-out bus (pan2 (+ (* amp echo) source) 0))))

(definst talking-bass [freq 130 mod1-partial 2.34 mod1-index 3 mod2-partial 12 mod2-index 30
                       car-partial 0.15 amp 1 dur 1.0]
 (let [mod2 (-> (sin-osc (* freq mod2-partial))
                (mul-add (* freq mod2-index) 0))
       lfo (-> (sin-osc 1.25) (mul-add 1 0.9))
       mod1 (-> (sin-osc (+ mod2 (* freq mod1-partial lfo)))
                (mul-add (* freq mod1-index) 0))
       carrier (sin-osc (+ mod1 (* freq car-partial)))]
   (-> carrier
       (* (env-gen (adsr 0.02 0.2 0.1 0.1) (line:kr 1 0 dur)))
       (* amp)
       pan2)))

(definst d-bass [freq 220 dur 1.0 res 1000 volume 1.0 pan 0 wet 0.33 room 0.5 cutoff 5000 env-amount 0.95]
   (let [fil-env (env-gen (adsr 0.1 0.9 0.1 0.6))]
     (-> (sin-osc [(/ freq 2) (+ (midicps 0.17) (/ freq 2)) (+ (midicps 0.15) (/ freq 2))])
         ;(tanh)
         (lpf (+ cutoff (* env-amount fil-env)))
         (* (env-gen (adsr 0.1 0.4 0.4 0.4) (line:kr 1 0 dur)))
         (* volume)
         pan2)))

(definst g-bass [freq 110 dur 1.0 res 5000 volume 1.0 pan 0 wet 0.2 room 0.4 cutoff 2000 env-amount 0.55]
   (let [mod (-> (sin-osc (* freq 1.5))
                 (mul-add (* freq 5.5) 50))
         lfo (-> (sin-osc 2) (mul-add 1 0.75))
         fil-env (env-gen (perc 0.3 dur))]
     (-> (sin-osc (+ (* lfo mod) (/ freq 4)))
         ;(clip2 0.8)
         (rlpf (+ 20 (* env-amount cutoff fil-env)) 1/7)
         (* (env-gen (adsr 0.02 0.2 0.1 0.1) (line:kr 1 0 dur)))
         ;(pan2)
         (effects :pan pan :wet wet :room room :volume volume))))

(definst supersaw [freq 440 duration 0.2 amp 1.75 cutoff 10000 env-amount 0.5]
   (let [snd-fn (fn [freq osc]
                  (let [tune (ranged-rand 2 32)]
                    (osc (+ freq tune))))
         saws (mix (repeatedly 13 #(snd-fn freq saw)))
         subs (mix (repeatedly 5 #(snd-fn (/ freq 4) pulse)))
         snd  (+ (* 1.1 saws) (* 0.45 subs))
         fil-env (env-gen (adsr 0.1 0.9 0.1 0.6) (line:kr 1 0 duration))
         snd (rlpf snd (+ cutoff (* env-amount fil-env)) 0.65)
         env (env-gen (adsr 0.1 0.45 0.20) (line:kr 1 0 duration) :action FREE)]
     (pan2 (* snd env amp))))

(defsynth dub [freq 440 divisor 2.0 depth 2.0 out-bus 0 duration 1 bpm 120 wobble 5/2]
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
    (out out-bus (pan2 (* env (+ wob (* 0.8 verb)))))))

(definst dark-bass [freq 110 volume 2.0 dur 1.0]
   (let [subfreq (/ freq 2)]
     (->
       (sin-osc [subfreq (- subfreq 2) (+ subfreq 2)])
       tanh
       (* (env-gen (perc 0 dur)))
       (* (x-line:kr 1 0.6 0.1))
       (* volume)
       pan2)))

(definst indie-bass [freq 440 dur 1.0 amp 1.0 pan 0 wet 0.2 room 0.9 cutoff 5000 fil-dur 0.5]
   (let [envelope (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur))
         level (+ 1000 (env-gen (perc 0 fil-dur) :level-scale cutoff))
         osc (mix [(saw freq)
                   (saw (* freq 1.009))
                   (pulse (/ freq 2) 0.5)])]
     (-> osc
         (lpf level)
         (* envelope)
         (effects :pan pan :wet wet :room room :volume amp))))

(definst wide-bass [freq 220 dur 1.0 cutoff 1500 sub-amp 0.4 amp 0.9]
   (let [osc1 (saw freq)
         osc2 (saw (* freq 0.99))
         sub  (* sub-amp (pulse (/ freq 2)))
         snd  (mix [osc1 osc2 sub])
         snd  (lpf snd (+ 100 (env-gen (perc 0 1.5) :level-scale cutoff)))
         env  (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur))]
      (pan2 (* snd env amp))))

(definst bass [freq 220 dur 1.0 amp 0.5 osc-mix 0.2 cutoff 2500 fil-amt 100]
   (let [sub-freq (/ freq 2)
         osc1 (saw:ar freq)
         osc2 (pulse sub-freq 0.5)
         osc (+ (* osc-mix osc2) (* (- 1 osc-mix) osc1))
         snd [osc osc]
         level (+ fil-amt (env-gen (perc 0 dur) :level-scale cutoff))
         snd (rlpf snd level 0.6)
         env (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur))]
     (out 0 (* amp env snd))))

(definst bend-noise [amp 0.7 decay 0.85 cutoff 0.65]
   (let [osc (white-noise)
         snd [osc osc]
         snd (bpf snd (lin-exp cutoff 0.0 1.0 20.0 20000.0) 0.5)
         env (env-gen (env-adsr 0.0 decay 0.0 0.7) :action FREE)]
     (out 0 (* amp env snd))))

(definst pad [freq 220 dur 1.0 amp 0.5 pan 0 cutoff 2500 fil-amt 100]
   (let [env (env-gen (asr 0 1 1) (line:kr 1.0 0.0 dur))
         fil-env (+ fil-amt (env-gen (perc 0 dur) :level-scale cutoff))
         osc (sin-osc freq)]
     (-> osc
         (lpf fil-env)
         (* env amp)
         (echo)
         pan2)))

(definst strings [freq 220 sustain 1.7 release 0.4 amp 1 cutoff 0.1 contour 0.3]
   (let [freqs [freq (* 0.99 freq) (* 1.10 freq)]
         src   (saw freqs)
         fil-env (env-gen (adsr 0.1 0.9 0.1 0.6))
         snd   (bpf src (+ (* fil-env (* contour 10000))
                           (lin-exp cutoff 0.0 1.0 20.0 20000.0)))
         env   (env-gen (env-lin 0.01 sustain release) 1 1 0 1 FREE)]
     (out 0 (pan2 (* amp env snd)))))

(comment
  (stop)
  (bend-noise)
  (pad)
  (strings)
  (bass :freq 220)
  (wide-bass :freq 220)
  (indie-bass :freq 220)
  (supersaw :cutoff 5000 :env-amount 0.9)
  (g-bass :freq 220 :cutoff 5000 :env-amount 0.1)
  (d-bass)
  (dark-bass 440)
  (dub 100))
