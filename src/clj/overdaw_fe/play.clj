(ns overdaw-fe.play
  (:require [leipzig.live :as live]
            [leipzig.temperament :refer [equal]]
            [overdaw-fe.inst :as i]))

(def controls (atom {:bass {:cutoff 2250}}))

(defn to-args [m]
  (mapcat vec m))

(defn play [name params]
  (live/play-note (merge params {:part name :pitch (equal (:note params))})))

(defmethod live/play-note :supersaw [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/supersaw :freq hertz :cutoff 4000 :env-amount 500 :duration seconds :amp (or amp 1))))

(defmethod live/play-note :bass [{hertz :pitch seconds :duration amp :amp :as params}]
  (when hertz
    (let [params {:freq hertz :dur seconds :volume (or amp 1)}]
      (apply i/bass (to-args (merge params (:bass @controls)))))))

(defmethod live/play-note :indie-bass [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/indie-bass :freq hertz :dur seconds :amp (or amp 1) :room 0.7 :damp 0.7)))

(defmethod live/play-note :wide-bass [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/wide-bass :freq hertz :dur seconds :amp (or amp 1))))

(defmethod live/play-note :garage-bass [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/g-bass :freq hertz :dur seconds :amp (or amp 1))))

(defmethod live/play-note :pad [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/pad :freq hertz :dur seconds :amp (or amp 1))))

(defmethod live/play-note :talking-bass [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/talking-bass :freq hertz :dur seconds :amp (or amp 1))))

(defmethod live/play-note :g-bass [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/g-bass :freq hertz :dur seconds :amp (or amp 1))))

(defmethod live/play-note :strings [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/strings :freq hertz :dur seconds :amp (or amp 1))))

(defmethod live/play-note :dark-bass [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/dark-bass :freq hertz :dur seconds :amp (or amp 1))))