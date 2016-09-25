(ns disclojure-ui.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [disclojure-ui.core-test]))

(doo-tests 'overdaw-fe.core-test)
