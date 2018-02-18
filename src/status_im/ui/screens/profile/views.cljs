(ns status-im.ui.screens.profile.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.profile.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.utils :as utils]
            [status-im.utils.core :refer [hash-tag?]]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [status-im.protocol.core :as protocol]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]))

(defn my-profile-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [react/touchable-highlight
    {:on-press #(re-frame/dispatch [:my-profile/start-editing-profile])}
    [react/view
     [react/text {:style      common.styles/label-action-text
                  :uppercase? components.styles/uppercase?} (i18n/label :t/edit)]]]])

(defn my-profile-edit-toolbar []
  [toolbar/toolbar {}
   nil
   [toolbar/content-title ""]
   [toolbar/default-done {:handler   #(re-frame/dispatch [:my-profile/save-profile])
                          :icon      :icons/ok
                          :icon-opts {:color colors/blue}}]])

(defn profile-contact-toolbar [contact]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]
   [toolbar/actions
    (when (and (not (:pending? contact))
               (not (:unremovable? contact)))
      [(actions/opts [{:action #(re-frame/dispatch [:hide-contact contact])
                       :label  (i18n/label :t/remove-from-contacts)}])])]])

(def profile-icon-options
  [{:label  (i18n/label :t/image-source-gallery)
    :action #(re-frame/dispatch [:my-profile/update-picture])}
   {:label  (i18n/label :t/image-source-make-photo)
    :action (fn []
              (re-frame/dispatch [:request-permissions
                                  [:camera :write-external-storage]
                                  #(re-frame/dispatch [:navigate-to :profile-photo-capture])
                                  #(utils/show-popup (i18n/label :t/error)
                                                     (i18n/label :t/camera-access-error))]))}])



(defn profile-actions [{:keys [pending? whisper-identity dapp?]} chat-id]
  [react/view action-button.styles/actions-list
   (if pending?
     [action-button/action-button {:label     (i18n/label :t/add-to-contacts)
                                   :icon      :icons/add
                                   :icon-opts {:color :blue}
                                   :on-press  #(re-frame/dispatch [:add-pending-contact chat-id])}]
     [action-button/action-button-disabled {:label (i18n/label :t/in-contacts) :icon :icons/ok}])
   [action-button/action-separator]
   [action-button/action-button {:label     (i18n/label :t/start-conversation)
                                 :icon      :icons/chats
                                 :icon-opts {:color :blue}
                                 :on-press  #(re-frame/dispatch [:profile/send-message whisper-identity])}]
   (when-not dapp?
     [react/view
      [action-button/action-separator]
      [action-button/action-button {:label     (i18n/label :t/send-transaction)
                                    :icon      :icons/arrow-right
                                    :icon-opts {:color :blue}
                                    :on-press  #(re-frame/dispatch [:profile/send-transaction chat-id whisper-identity])}]])])

(defn profile-info-item [{:keys [label value options text-mode empty-value? accessibility-label]}]
  [react/view styles/profile-setting-item
   [react/view (styles/profile-info-text-container options)
    [react/text {:style styles/profile-info-title}
     label]
    [react/view styles/profile-setting-spacing]
    [react/text {:style               (if empty-value?
                                        styles/profile-setting-text-empty
                                        styles/profile-setting-text)
                 :number-of-lines     1
                 :ellipsizeMode       text-mode
                 :accessibility-label accessibility-label}
     value]]
   (when options
     [react/touchable-highlight {:on-press #(list-selection/show {:options options})}
      [react/view profile.components.styles/modal-menu
       [vector-icons/icon :icons/options {:container-style styles/profile-info-item-button}]]])])

(defn- toolbar [label value]
  [toolbar/toolbar {}
   [toolbar/default-done {:icon-opts {:color colors/black}}]
   [toolbar/content-title label]
   [toolbar/actions [{:icon      :icons/share
                      :icon-opts {:color :black}
                      :handler   #(list-selection/open-share {:message value})}]]])

(defview qr-viewer []
  (letsubs [{:keys [value contact]} [:get :qr-modal]]
    [react/view {:flex-grow      1
                 :flex-direction :column}
     [status-bar/status-bar {:type :modal}]
     [toolbar (:name contact) value]
     [qr-code-viewer/qr-code-viewer {}
      value (i18n/label :t/qr-code-public-key-hint) (str value)]]))

(defn- show-qr [contact source value]
  #(re-frame/dispatch [:navigate-to :profile-qr-viewer {:contact contact
                                                        :source  source
                                                        :value   value}]))

(defn profile-options [text]
  (into []
        (when text
          (list-selection/share-options text))))

(defn profile-info-address-item [address]
  [profile-info-item
   {:label               (i18n/label :t/address)
    :action              address
    :options             (profile-options address)
    :text-mode           :middle
    :accessibility-label :profile-address
    :value               address}])

(defn profile-info-public-key-item [whisper-identity]
  [profile-info-item
   {:label               (i18n/label :t/public-key)
    :action              whisper-identity
    :options             (profile-options whisper-identity)
    :text-mode           :middle
    :accessibility-label :profile-public-key
    :value               whisper-identity}])

(defn settings-item-separator []
  [common/separator styles/settings-item-separator])

(defn tag-view [tag]
  [react/text {:style {:color colors/blue}
               :font  :medium}
   (str tag " ")])

(defn settings-title [title]
  [react/text {:style styles/profile-settings-title}
   title])

(defn settings-item [label-kw value action-fn active?]
  [react/touchable-highlight
   {:on-press action-fn
    :disabled (not active?)}
   [react/view styles/settings-item
    [react/text {:style styles/settings-item-text}
     (i18n/label label-kw)]
    [react/text {:style      styles/settings-item-value
                 :uppercase? components.styles/uppercase?} value]
    (when active?
      [vector-icons/icon :icons/forward {:color colors/gray}])]])

(defn profile-info [{:keys [whisper-identity address]}]
  [react/view
   [profile-info-address-item address]
   [settings-item-separator]
   [profile-info-public-key-item whisper-identity]])

(defn navigate-to-accounts []
  ;; TODO(rasom): probably not the best place for this call
  (protocol/stop-whisper!)
  (re-frame/dispatch [:navigate-to :accounts]))

(defn handle-logout []
  (utils/show-confirmation (i18n/label :t/logout-title)
                           (i18n/label :t/logout-are-you-sure)
                           (i18n/label :t/logout) navigate-to-accounts))

(defn logout []
  [react/view {}
   [react/touchable-highlight
    {:on-press handle-logout}
    [react/view styles/settings-item
     [react/text {:style styles/logout-text
                  :font  (if platform/android? :medium :default)}
      (i18n/label :t/logout)]]]])

(defn my-profile-settings [{:keys [network networks]}]
  [react/view
   [settings-title (i18n/label :t/settings)]
   [settings-item :t/main-currency "USD" #() false]
   [settings-item-separator]
   [settings-item :t/notifications "" #() true]
   [settings-item-separator]
   [settings-item :t/network (get-in networks [network :name])
    #(re-frame/dispatch [:navigate-to :network-settings]) true]
   (when config/offline-inbox-enabled?
     [settings-item-separator])
   (when config/offline-inbox-enabled?
     [settings-item :t/offline-messaging-settings ""
      #(re-frame/dispatch [:navigate-to :offline-messaging-settings]) true])])

(defn network-info []
  [react/view styles/network-info
   [common/network-info]
   [common/separator]])

(defn share-contact-code [current-account public-key]
  [react/touchable-highlight {:on-press (show-qr current-account :public-key public-key)}
   [react/view styles/share-contact-code
    [react/view styles/share-contact-code-text-container
     [react/text {:style      styles/share-contact-code-text
                  :uppercase? components.styles/uppercase?}
      (i18n/label :t/share-contact-code)]]
    [react/view styles/share-contact-icon-container
     [vector-icons/icon :icons/qr {:color colors/blue}]]]])

(defview my-profile []
  (letsubs [{:keys [public-key] :as current-account} [:get-current-account]
            editing?                                 [:get :my-profile/editing?]
            changed-account                          [:get :my-profile/profile]]
    (let [shown-account (merge current-account changed-account)]
      [react/view styles/profile
       (if editing?
         [my-profile-edit-toolbar]
         [my-profile-toolbar])
       [react/scroll-view
        [react/view styles/profile-form
         [profile.components/profile-header shown-account editing? profile-icon-options :my-profile/update-name]]
        [react/view action-button.styles/actions-list
         [share-contact-code current-account public-key]]
        [react/view styles/profile-info-container
         [my-profile-settings current-account]]
        [logout]]])))

(defview profile []
  (letsubs [contact [:contact]
            chat-id [:get :current-chat-id]]
    [react/view styles/profile
     [status-bar/status-bar]
     [profile-contact-toolbar contact]
     [network-info]
     [react/scroll-view
      [react/view styles/profile-form
       [profile.components/profile-header contact false nil nil]]
      [common/form-spacer]
      [profile-actions contact chat-id]
      [common/form-spacer]
      [react/view styles/profile-info-container
       [profile-info contact]
       [common/bottom-shadow]]]]))

;;; MOCK CODE BELOW \/

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

(def action-section-style
  {:background-color colors/white})

(def action-style
  {:background-color components.styles/color-blue4-transparent
   :border-radius    50})

(def action-label
  {:color colors/blue})

(def action-separator
  {:height           1
   :background-color colors/white-light-transparent
   :margin-left      70})

(def action-icon-opts
  {:color colors/blue})

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
      [react/view styles/profile
       (if editing?
         [group-chat-profile-edit-toolbar]
         [group-chat-profile-toolbar])
       [react/scroll-view
        [react/view styles/profile-form
         [profile.components/profile-header shown-chat editing? nil :set-group-chat-name]
         [list/action-list actions
          {:container-style    action-section-style
           :action-style       action-style
           :action-label-style action-label
           :icon-opts          action-icon-opts}]]]])))
