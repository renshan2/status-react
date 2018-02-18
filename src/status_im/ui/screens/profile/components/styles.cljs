(ns status-im.ui.screens.profile.components.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]))

(def profile-header-display
  {:flex-direction  :column
   :justify-content :center
   :align-items     :center})

(def profile-header-edit
  {:flex-direction  :column
   :justify-content :center})

(defstyle profile-name-text
  {:padding-vertical 14
   :font-size        15
   :text-align       :center
   :ios              {:letter-spacing -0.2}
   :android          {:color colors/black}})

(defstyle profile-name-input-text
          {:font-size  15
           :text-align :center
           :flex       1
           :ios        {:letter-spacing      -0.2
                        :height              46
                        :border-bottom-width 1
                        :border-bottom-color styles/color-light-gray3}
           :android    {:color               colors/black
                        :border-bottom-width 2
                        :border-bottom-color styles/color-blue4}})

(def profile-header-name-container
  {:flex            1
   :justify-content :center})


(def modal-menu
  {:align-items :center})