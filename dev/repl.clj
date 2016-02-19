(ns repl
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [overdaw-fe.system :as system]
            [overdaw-fe.live :as l]))

(def config
  {:http {:port 3000}, :dev-mode? true})

(reloaded.repl/set-init! #(system/new-system config))

(def alter-raw-track (partial l/alter-raw-track (get reloaded.repl/system :state)))

(def commit-track (partial l/commit-track (get reloaded.repl/system :state)))
