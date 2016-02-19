(ns overdaw-fe.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [plumbing.core :refer [map-vals]]
            [overdaw-fe.config :refer [res]]))

(re-frame/register-sub
 :name
 (fn [db]
   (reaction (:name @db))))

(re-frame/register-sub
  :track-part
  (fn [db [_]]
    (let [instr (reaction (get-in @db [:track-controls :instr]))]
      (reaction (-> (:track @db) (get @instr))))))

(re-frame/register-sub
  :beat
  (fn [db]
    (reaction
      (->> (-> (:track @db) (get "beat") vals first)
           (map #(assoc % :duration 0.25))
           (group-by :drum)))))

(re-frame/register-sub
  :notes
  (fn [db] (reaction (:notes @db))))

(re-frame/register-sub
  :kit
  (fn [db] (reaction (:kit @db))))

(re-frame/register-sub
  :instruments
  (fn [db] (reaction (:instruments @db))))

(re-frame/register-sub
  :track-controls
  (fn [db] (reaction (:track-controls @db))))