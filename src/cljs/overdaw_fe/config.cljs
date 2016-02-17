(ns overdaw-fe.config)

(def debug?
  ^boolean js/goog.DEBUG)

(when debug?
  (enable-console-print!))

(def api-base "/api")

(def semitones 18)
(def ticks (* 8 8))
(def res 0.25)
