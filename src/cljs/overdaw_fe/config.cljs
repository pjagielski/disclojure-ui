(ns overdaw-fe.config)

(def debug?
  ^boolean js/goog.DEBUG)

(when debug?
  (enable-console-print!))

(def api-base
  "http://localhost:3000/api")
