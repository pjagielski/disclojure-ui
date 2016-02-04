(ns overdaw-fe.model
  (:require [schema.core :as s]))

(def MutationType (s/enum :add :remove))

(s/defschema TrackMutation
  {:part     String
   :time     Number
   :pitch    Long
   :duration Number
   :amp      Number
   :type     MutationType})

(s/defschema BeatMutation
  {:time     Number
   :drum     String
   :amp      Number
   :duration Number
   :type     MutationType})

(defn add? [mutation]
  (= :add (:type mutation)))