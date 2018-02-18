(ns status-im.ui.screens.profile.group-chat.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.profile.group-chat.style :as style]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [re-frame.core :as re-frame]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.i18n :as i18n]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
  :group-chat-profile/start-editing
  (fn [{:keys [db]} []]
    {:db (assoc db :group-chat-profile/editing? true)}))

(handlers/register-handler-fx
  :group-chat-profile/save-profile
  (fn [{:keys [db]} _]
    (let [{:accounts/keys [accounts current-account-id]} db]
      (-> {:db db}
          (update :db dissoc :group-chat-profile/editing?)))))

(defn group-chat-profile-toolbar []
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]
   [react/touchable-highlight
    {:on-press #(re-frame/dispatch [:group-chat-profile/start-editing])}
    [react/view
     [react/text {:style      common.styles/label-action-text
                  :uppercase? components.styles/uppercase?} (i18n/label :t/edit)]]]])

(defn group-chat-profile-edit-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [toolbar/default-done {:handler   #(re-frame/dispatch [:group-chat-profile/save-profile])
                          :icon      :icons/ok
                          :icon-opts {:color colors/blue}}]])

(def actions
  [{:label  "Add members"
    :icon   :icons/add
    :action #(js/alert "add members")}
   {:label  "Clear history"
    :icon   :icons/close
    :action #(js/alert "clear history")}
   {:label  "Leave group"
    :icon   :icons/arrow-left
    :action #(js/alert "leave group")}])

(defview group-chat-profile []
  (letsubs [current-chat [:get-current-chat]
            editing?     [:get :group-chat-profile/editing?]
            changed-chat [:get :group-chat-profile/profile]]
    (let [shown-chat (merge current-chat changed-chat)]
      [react/view profile.components.styles/profile
       (if editing?
         [group-chat-profile-edit-toolbar]
         [group-chat-profile-toolbar])
       [react/scroll-view
        [react/view profile.components.styles/profile-form
         [profile.components/profile-header shown-chat editing? nil :set-group-chat-name]
         [list/action-list actions
          {:container-style    style/action-container
           :action-style       style/action
           :action-label-style style/action-label
           :icon-opts          style/action-icon-opts}]]]])))