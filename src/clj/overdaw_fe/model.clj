(ns overdaw-fe.model
  (:require [schema.core :as s]))

(def MutationType (s/enum :add :remove))

(s/defschema TrackMutation
  {:part  String
   :time  Number
   :pitch Long
   :type  MutationType})

(s/defschema BeatMutation
  {:time Number
   :drum String
   :type MutationType})

(defn add? [mutation]
  (= :add (:type mutation)))