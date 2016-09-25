(ns disclojure-ui.runtime)

(defn find-instruments [ns]
  (->> (ns-interns ns)
       (filter (fn [e] (= :overtone.studio.inst/instrument
                          (type (val e)))))
       (map key)))