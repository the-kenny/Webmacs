(ns webmacs.pages.buffer
  (:use noir.core
        noir-async.core
        aleph.http
        lamina.core
        hiccup.core
        hiccup.page-helpers)
  (:require [noir.server :as server]
            [webmacs.publishers :as publishers]
            [webmacs.buffer :as buffer]))

(defpage "/emacs/:name" {:keys [name]}
  (html5 [:head
          [:title "Emacs: " name]
          (include-js "/js/main.js")
          (include-css "/css/main.css")]
         [:body
          [:div
           [:table {:class "code-table"}
            [:tbody
             [:tr
              [:td
               [:pre {:id "line-numbers"}]]
              [:td {:width "100%"}
               [:pre {:id "buffer-contents"}]]]]]
           ;; [:pre {:id "buffer-contents"}]
           [:pre {:id "mode-line"}]]
          (javascript-tag ;; "(open-socket \"ws://localhost:3000/sockets/\")"
           (str "webmacs.core.open_socket(\"ws://\"+window.location.host+\"/sockets/" name "\")"))]))

(defwebsocket "/sockets/:sname" {:keys [sname]} conn
  ;;`send-message' is equal to `enqueue', so `conn' must be a channel
  ;; TODO: Move this out of here
  ;; TODO: Remove client on close
  (let [chan (permanent-channel)]
    (receive-all chan (fn [msg] (enqueue (:request-channel conn) (str msg))))
   (publishers/add-client! sname chan))

  (on-receive conn (fn [msg] (println "Got a message: " msg)))
  (on-close conn (fn [] (println "Socket down!"))))
