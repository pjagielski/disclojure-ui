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
  (fn [db [_ instr pitch]]
    (reaction (-> (:track @db) (get instr) (get pitch)))))

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
  :instr-controls
  (fn [db] (reaction (:instr-controls @db))))

(re-frame/register-sub
  :controls
  (fn [db] (reaction (:controls @db))))

(re-frame/register-sub
  :configs
  (fn [db] (reaction (:configs @db))))

(re-frame/register-sub
  :editor
  (fn [db] (reaction (:editor @db))))

(re-frame/register-sub
  :instr
  (fn [db] (reaction (:instr @db))))