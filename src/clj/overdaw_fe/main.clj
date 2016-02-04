(ns overdaw-fe.main
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [overdaw-fe.system :refer [new-system]]))

(def config
  {:http {:port 3000}, :dev-mode? false})

(defn -main [& args]
  (let [system (new-system config)]
    (println "Starting HTTP server on port" (-> system :http :port))
    (component/start system)))
