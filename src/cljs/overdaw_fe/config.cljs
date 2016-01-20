(ns overdaw-fe.config)

(def debug?
  ^boolean js/goog.DEBUG)

(when debug?
  (enable-console-print!))

(def api-base
  "http://localhost:3000/api")

(def from 68)
(def semitones 24)
(def notes 64)
(def res 0.25)
