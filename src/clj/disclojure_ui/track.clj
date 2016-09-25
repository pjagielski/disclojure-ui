(ns disclojure-ui.track
  (:require [disclojure.play]
            [disclojure.track :as t]))

(def initial-track {:beat []})

(reset! t/metro 120)
