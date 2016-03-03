(ns overdaw-fe.kit
  (:require [overtone.live :as o]
            [clojure.java.io :as io]))

(defn without-extension [filename]
  (subs filename 0 (.lastIndexOf filename ".")))

(defn make-kit [kit]
  (->>
    (.listFiles (io/file (io/resource (str "sounds/kits/" kit))))
    (filter #(.endsWith (.getName %) ".wav"))
    (map (fn [f] [(-> (without-extension (.getName f)) keyword)
                  {:sound (o/sample (.getAbsolutePath f))
                   :amp   1}]))
    (into (sorted-map))))
