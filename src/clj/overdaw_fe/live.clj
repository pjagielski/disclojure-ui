(ns overdaw-fe.live
  (:require [overdaw-fe.track :as t]))

(defn alter-raw-track [raw-track-ref instr new-track]
  (dosync
    (alter raw-track-ref assoc instr new-track)))

(defn commit-track [raw-track-ref track-ref]
  (dosync
    (ref-set track-ref (t/track @raw-track-ref))))