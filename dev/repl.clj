(ns repl
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [overdaw-fe.system :as system]
            [overdaw-fe.live :as l]
            [overdaw-fe.track :as t]))

(def config
  {:http {:port 3000}, :dev-mode? true})

(reloaded.repl/set-init! #(system/new-system config))

(defn alter-raw-track [instr new-track]
  (l/alter-raw-track t/raw-track instr new-track))

(defn commit-track []
  (let [{:keys [track raw-track]} (get reloaded.repl/system :state)]
    (l/commit-track raw-track track)))
