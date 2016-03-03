(ns overdaw-fe.db)

(def default-db
  {:track-controls {:duration 0.25 :amp 1 :from 32 :instr "bass" :bars 8
                    "bass" {:cutoff 1000}}
   :instruments [] :kit []
   :beat-controls {:amp 1}})
