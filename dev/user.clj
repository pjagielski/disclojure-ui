(ns user)

(defn dev
  "Load and switch to the 'dev' namespace."
  []
  (require 'repl)
  (in-ns 'repl)
  :loaded)
