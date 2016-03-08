(ns overdaw-fe.config)

(def debug?
  ^boolean js/goog.DEBUG)

(when debug?
  (enable-console-print!))

(def api-base "/api")

(def res 0.25)
