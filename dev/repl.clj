(ns repl
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [overdaw-fe.system :as system]
            [leipzig.melody :refer :all]))

(def config
  {:http {:port 3005}, :dev-mode? true})

(reloaded.repl/set-init! #(system/new-system config))
