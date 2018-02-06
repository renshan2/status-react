(ns status-im.thread
  (:require [taoensso.timbre :as log]))

(def Thread (.-Thread (js/require "react-native-threads")))

(defonce thread (atom nil))

(defn set-on-message! []
  (set! (.-onmessage @thread)
        (fn [m]
          (log/info :rn-thread-message m))))

(defn init []
  (reset! thread (Thread. "worker.thread.js"))
  (set-on-message!))

(defn post [message]
  (.postMessage @thread message))

(defn terminate []
  (.terminate @thread)

  (reset! thread nil))
