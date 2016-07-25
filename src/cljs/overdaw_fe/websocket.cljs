(ns overdaw-fe.websocket
  (:require-macros
    [cljs.core.async.macros :refer (go go-loop)])
  (:require
    [cljs.core.async :refer (<! >! put! chan)]
    [cljs.core.match :refer-macros [match]]
    [taoensso.sente :as sente]
    [re-frame.core :as re-frame]))

(defn event-handler [event]
  (.log js/console "Event: %s" (pr-str event))
  (match [event]
         [[:chsk/handshake _]] (.log js/console "Sente handshake")
         [[:chsk/recv [:disclojure/track track]]] (re-frame/dispatch [:push-track track])
         :else (.log js/console "Unmatched event: %s" (pr-str event))))

(defn event-loop [ch-recv]
  (go-loop []
           (let [{:as ev-msg :keys [event]} (<! ch-recv)]
             (event-handler event)
             (recur))))

(defn connect []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket! "/chsk" {:type :auto})]
    (.log js/console "Websocket connected")
    (event-loop ch-recv)))

(defonce connection (connect))
