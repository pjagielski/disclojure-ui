(ns overdaw-fe.config)

(def debug?
  ^boolean js/goog.DEBUG)

(when debug?
  (enable-console-print!))

(def api-base
  "http://localhost:3000/api")

(def from 36)
(def semitones 24)
(def notes 32)
(def res 0.25)
