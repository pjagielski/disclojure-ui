(ns overdaw-fe.handler
  (:require [clojure.java.io :as io]
            [plumbing.core :as p]
            [compojure.core :refer [routes context GET]]
            [compojure.route :as route]
            [compojure.api.sweet :refer [api context* defroutes* swagger-ui swagger-docs GET* POST* PUT*]]
            [ring.util.http-response :refer [ok]]
            [overdaw-fe.track :as t]
            [overdaw-fe.model :as m]
            [overdaw-fe.runtime :as r]
            [overdaw-fe.play :as pl]
            [overdaw-fe.live :as l]
            [overtone.live :as o]
            [leipzig.live :as live]))

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

(defn all-but-part [by-part part]
  (-> by-part
      (select-keys (remove (partial = part) (keys by-part)))
      vals
      flatten))

(defn reset-track [track-ref raw-track-ref new-track]
  (ref-set raw-track-ref new-track)
  (ref-set track-ref (t/track new-track)))

(p/defnk create [[:state track raw-track kit controls]]
  (routes
    (route/resources "/")
    (GET "/" []
      (io/resource "public/index.html"))
    (context "/api" []
      (api
        (swagger-ui "/swagger"
                    :swagger-docs "/api/swagger.json")
        (swagger-docs "/swagger.json"
                      {:info {:title "OverDAW api"} :basePath "/api"})
        (context "/track" []
          (GET* "/" [] (ok @raw-track))
          (POST* "/play" [] (live/jam track) (ok))
          (POST* "/stop" [] (live/stop) (ok))
          (PUT* "/" []
                :body [body m/TrackMutation]
                (dosync
                  (let [part (keyword (:part body))]
                    (as->
                      (get @raw-track part) $
                      (mutate-track! $ body part (m/add? body))
                      (l/alter-raw-track raw-track part $)))
                  (l/commit-track raw-track track)
                  (ok))))
        (context "/instruments" []
          (GET* "/" []
                (ok (r/find-instruments (symbol "overdaw-fe.inst"))))
          (POST* "/play" []
                 :body [body m/PlayNote]
                 (pl/play (keyword (:instr body)) body)
                 (ok)))
        (GET* "/notes/:from/:to" []
              :path-params [from :- Long, to :- Long]
              (->> (range from to)
                   (map (fn [x] {x (o/find-note-name x)}))
                   (into {})
                   ok))
        (context "/kit" []
          (GET* "/" []
                (ok (p/map-vals #(select-keys % [:amp]) @kit)))
          (POST* "/play" []
                 :body [body m/PlayBeat]
                 (apply (-> (get @kit (keyword (:drum body))) :sound) [])
                 (ok)))
        (context "/beat" []
          (PUT* "/" []
                :body [body m/BeatMutation]
                (dosync
                  (as->
                    (get @raw-track :beat) $
                    (mutate-beat! $ body (keyword (:drum body)) (m/add? body))
                    (l/alter-raw-track raw-track :beat $))
                  (l/commit-track raw-track track)
                  (ok))))
        (context "/controls" []
          (PUT* "/" []
                :body [body m/ControlChange]
                (let [instr (keyword (:instr body))
                      control (keyword (:control body))]
                  (swap! controls assoc-in [instr control] (:value body)))
                (ok)))))))