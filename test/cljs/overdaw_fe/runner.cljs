(ns overdaw-fe.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [overdaw-fe.core-test]))

(doo-tests 'overdaw-fe.core-test)
