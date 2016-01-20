(ns repl
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [overdaw-fe.system :as system]))

(def config
  {:http {:port 3000}, :dev-mode? true})

(reloaded.repl/set-init! #(system/new-system config))
