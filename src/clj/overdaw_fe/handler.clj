(ns overdaw-fe.handler
  (:require [clojure.java.io :as io]
            [plumbing.core :as p]
            [compojure.core :refer [routes context GET]]
            [compojure.route :as route]
            [compojure.api.sweet :refer [api context* defroutes* swagger-ui swagger-docs GET* POST* PUT*]]
            [ring.util.http-response :refer [ok]]
            [overdaw-fe.track :as t]
            [overdaw-fe.model :as m]
            [overtone.live :as o]
            [leipzig.live :as live]))

; -> cljc
(defn mutate-beat! [current {:keys [time duration amp]} drum add?]
  (if add?
    (conj current {:time time :duration duration :drum drum :part :beat :amp amp})
    (remove #(and (= (double time) (double (:time %))) (= drum (:drum %))) current)))

(defn mutate-track! [current {:keys [time duration amp pitch]} part add?]
  (if add?
    (conj current {:time time :duration duration :pitch pitch :part part :amp amp})
    (remove #(and (= (double time) (double (:time %))) (= pitch (:pitch %))) current)))

(defn all-but-part [by-part part]
  (-> by-part
      (select-keys (remove (partial = part) (keys by-part)))
      vals
      flatten))

(defn reset-track [track-ref raw-track-ref new-track]
  (ref-set raw-track-ref new-track)
  (ref-set track-ref (t/track new-track)))

(p/defnk create [[:track track raw-track kit]]
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
        (GET* "/track" [] (ok @raw-track))
        (POST* "/play" [] (live/jam track) (ok))
        (POST* "/stop" [] (live/stop) (ok))
        (GET* "/notes/:from/:to" []
              :path-params [from :- Long, to :- Long]
              (->> (range from to)
                   (map (fn [x] {x (o/find-note-name x)}))
                   (into {})
                   ok))
        (GET* "/kit" []
              (ok (->> @kit
                       (p/map-vals #(select-keys % [:amp])))))
        (PUT* "/track" []
              :body [body m/TrackMutation]
              (dosync
                (let [by-part (group-by :part @raw-track)
                      part (keyword (:part body))
                      curr-track (get by-part part)]
                  (->> (all-but-part by-part part)
                       (concat (mutate-track! curr-track body part (m/add? body)))
                       (sort-by :time)
                       (reset-track track raw-track))
                  (ok))))
        (PUT* "/beat" []
              :body [body m/BeatMutation]
              (dosync
                (let [by-part (group-by :part @raw-track)
                      curr-beat (get by-part :beat)]
                  (->> (all-but-part by-part :beat)
                       (concat (mutate-beat! curr-beat body (keyword (:drum body)) (m/add? body)))
                       (sort-by :time)
                       (reset-track track raw-track))
                  (ok))))))))
