(ns status-im.worker
  (:require [status-im.utils.name :as n]))

(def self (.-self (js/require "react-native-threads")))


(set! (.-onmessage self)
      (fn [message]
        ;; just to make sure that
        (.postMessage self (n/shortened-name message 2))))
