(ns status-im.ui.screens.profile.components.views
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.screens.profile.components.styles :as styles]))

(defn profile-name-input [name on-change-text-event]
  [react/view
   [react/text-input
    {:style          styles/profile-name-input-text
     :placeholder    ""
     :default-value  name
     :auto-focus     true
     :on-change-text #(when on-change-text-event
                        (re-frame/dispatch [on-change-text-event %]))}]])

(defn show-profile-icon-actions [options]
  (when (seq options)
    (list-selection/show {:title   (i18n/label :t/image-source-title)
                          :options options})))

(defn profile-header-display [{:keys [name] :as contact}]
  [react/view styles/profile-header-display
   [chat-icon.screen/my-profile-icon {:account contact
                                      :edit?   false}]
   [react/view styles/profile-header-name-container
    [react/text {:style           styles/profile-name-text
                 :number-of-lines 1}
     name]]])

(defn profile-header-edit [{:keys [name] :as contact}
                           icon-options on-change-text-event]
  [react/view styles/profile-header-edit
   [react/touchable-highlight {:on-press #(show-profile-icon-actions icon-options)}
    [react/view styles/modal-menu
     [chat-icon.screen/my-profile-icon {:account contact
                                        :edit?   true}]]]
   [react/view styles/profile-header-name-container
    [profile-name-input name on-change-text-event]]])

(defn profile-header [contact editing? options on-change-text-event]
  (if editing?
    [profile-header-edit contact options on-change-text-event]
    [profile-header-display contact]))