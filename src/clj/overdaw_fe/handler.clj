(ns overdaw-fe.handler
  (:require [clojure.java.io :as io]
            [plumbing.core :as p]
            [compojure.core :refer [routes context GET]]
            [compojure.route :as route]
            [compojure.api.sweet :refer [api context* defroutes* swagger-ui swagger-docs GET* POST* PUT*]]
            [ring.util.http-response :refer [ok]]
            [overdaw-fe.track :as t]
            [overtone.live :as o]
            [leipzig.live :as live]))

(p/defnk create [[:state counter]]
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
        (GET* "/track" [] (ok t/raw-track))
        (POST* "/play" [] (live/play t/track) (ok))
        (POST* "/stop" [] (live/stop) (ok))
        (GET* "/notes/:from/:to" []
              :path-params [from :- Long, to :- Long]
              (->> (range from to)
                   (map (fn [x] {x (o/find-note-name x)}))
                   (into {})
                   ok))))))
