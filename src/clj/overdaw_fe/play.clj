(ns overdaw-fe.play
  (:require [leipzig.live :as live]
            [overdaw-fe.inst :as i]))

(defmethod live/play-note :supersaw [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/supersaw :freq hertz :duration seconds :amp (or amp 1))))

(defmethod live/play-note :bass [{hertz :pitch seconds :duration amp :amp}]
  (when hertz
    (i/dub :freq hertz :duration seconds :volume (or amp 1))))
