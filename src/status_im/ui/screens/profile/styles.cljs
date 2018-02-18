(ns status-im.ui.screens.profile.styles
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def profile-info-item-button
  {:padding 16})

(def edit-name-title
  {:color   colors/gray
   :ios     {:font-size      13
             :letter-spacing -0.1}
   :android {:font-size 12}})

(defstyle profile-setting-item
  {:flex-direction :row
   :align-items    :center
   :padding-left   16
   :ios            {:height 73}
   :android        {:height 72}})

(defn profile-info-text-container [options]
  {:flex          1
   :padding-right (if options 16 40)})

(defstyle profile-info-title
  {:color         colors/gray
   :margin-left   16
   :font-size     14
   :ios           {:letter-spacing -0.2}})

(defstyle profile-setting-text
  {:ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 16
             :color     colors/black}})

(defstyle profile-setting-spacing
  {:ios     {:height 10}
   :android {:height 7}})

(def profile-setting-text-empty
  (merge profile-setting-text
         {:color colors/gray}))

(defstyle profile-name-input
  {:color   styles/text1-color
   :ios     {:font-size      17
             :padding-bottom 0
             :line-height    17
             :letter-spacing -0.2}
   :android {:font-size      16
             :line-height    16
             :padding-top    5
             :height         30
             :padding-bottom 0}})

(def network-info {:background-color :white})
