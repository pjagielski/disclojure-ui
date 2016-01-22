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
(defn mutate! [current part time drum add?]
  (if add?
    (conj current {:time time :duration (- 8 time) :drum drum :part part})
    (remove #(and (= time (:time %)) (= drum (:drum %))) current)))

(defn all-but-part [by-part part]
  (-> by-part
      (select-keys (remove (partial = part) (keys by-part)))
      vals
      flatten))

(p/defnk create [[:track track]]
  (routes
    (route/resources "/")
    (GET "/" []
      (io/resource "public/index.html"))
    (context "/api" []
      (api
        (swagger-ui "/swagger"
                    :swagger-docs "/api/swagger.json")
        (swagger-docs "/swagger.json"
                      {:info {:title "AASA api"} :basePath "/api"})
        (GET* "/track" [] (ok @track))
        (POST* "/play" [] (t/play @track) (ok))
        (POST* "/stop" [] (live/stop) (ok))
        (GET* "/notes/:from/:to" []
              :path-params [from :- Long, to :- Long]
              (->> (range from to)
                   (map (fn [x] {x (o/find-note-name x)}))
                   (into {})
                   ok))
        (PUT* "/track/beat" []
              :body [body m/BeatMutation]
              (dosync
                (let [by-part (group-by :part @track)
                      {:keys [drum time]} body
                      curr-beat (get by-part :beat)]
                  (->> (all-but-part by-part :beat)
                       (concat (mutate! curr-beat :beat time (keyword drum) (m/add? body)))
                       (sort-by :time)
                       (ref-set track))
                  (ok))))))))
