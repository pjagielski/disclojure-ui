(ns overdaw-fe.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:body {}]
  [:table.track
   [:td {:background-color "#eeeeee"
         :width "20px" :font-size "10px"
         :text-align   "center"}]
   [:td.key {:width "50px"}]
   [:td.x {:background-color "#000000"}]])
