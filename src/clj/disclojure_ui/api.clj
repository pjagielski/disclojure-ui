(ns disclojure-ui.api
  (:require [clojure.java.io :as io]
            [compojure.core :as c]
            [compojure.route :as route]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok]]
            [disclojure.live :as dl :refer [assoc-track]]
            [disclojure-ui.model :as m]
            [disclojure-ui.runtime :as r]
            [disclojure.play :as pl]
            [overtone.live :as o]
            [leipzig.live :as live]
            [plumbing.core :as p]))

; -> cljc
(defn mutate-beat! [current {:keys [time duration amp]} drum add?]
  (->>
    (if add?
      (conj current {:time time :duration duration :drum drum :part :beat :amp amp})
      (remove #(and (= (double time) (double (:time %))) (= drum (:drum %))) current))
    (sort-by :time)))

(defn mutate-track! [current {:keys [time duration amp pitch]} part add?]
  (->>
    (if add?
      (conj current {:time time :duration duration :pitch pitch :part part :amp amp})
      (remove #(and (= (double time) (double (:time %))) (= pitch (:pitch %))) current))
    (sort-by :time)))

(defn api-routes [{{:keys [raw-track kit controls]} :state}]
  (routes
    (route/resources "/")
    (c/GET "/" []
      (io/resource "public/index.html"))
    (api
      {:swagger {:ui "/api-docs" :spec "/swagger.json"
                 :data {:info {:version "1.0.0"
                               :title "Disclojure-UI API"
                               :description "Interact with Overtone via REST"}}}}
      (context "/api" []
        (context "/track" []
          (GET "/" [] (ok @raw-track))
          (POST "/play" [] (live/jam (dl/track)) (ok))
          (POST "/stop" [] (live/stop) (ok))
          (PUT "/" []
            :body [body m/TrackMutation]
            (let [part (keyword (:part body))]
              (as->
                (get @raw-track part) $
                (mutate-track! $ body part (m/add? body))
                (assoc-track part $)))
            (ok)))
        (context "/instruments" []
          (GET "/" []
            (->
              (symbol "disclojure.inst")
              (r/find-instruments)
              (ok)))
          (POST "/play" []
            :body [body m/PlayNote]
            (pl/play (keyword (:instr body)) body)
            (ok)))
        (GET "/notes/:from/:to" []
          :path-params [from :- Long, to :- Long]
          (->> (range from to)
               (map (fn [x] {x (o/find-note-name x)}))
               (into {})
               ok))
        (context "/kit" []
          (GET "/" []
            (ok (p/map-vals #(select-keys % [:amp]) @kit)))
          (POST "/play" []
            :body [body m/PlayBeat]
            (apply (-> (get @kit (keyword (:drum body))) :sound) [])
            (ok)))
        (context "/beat" []
          (PUT "/" []
            :body [body m/BeatMutation]
            (as->
              (get @raw-track :beat) $
              (mutate-beat! $ body (keyword (:drum body)) (m/add? body))
              (assoc-track :beat $))
            (ok)))
        (context "/controls" []
          (PUT "/" []
            :body [body m/ControlChange]
            (let [instr (keyword (:instr body))
                  control (keyword (:control body))]
              (swap! controls assoc-in [instr control] (:value body)))
            (ok)))))))
