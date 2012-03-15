(ns webmacs.server
  (:use noir.core
        noir-async.core
        aleph.http
        lamina.core
        hiccup.core
        hiccup.page-helpers)
  (:require [noir.server :as server]
            [webmacs.publishers :as publishers]
            [webmacs.buffer :as buffer]))

(defpage "/" []
  (html5 [:head
          [:title "Webmacs"]]
         [:body
          (if-let [buffers (seq (publishers/buffer-names))]
           [:ul
            (for [name buffers]
              [:li [:a {:href (str "/emacs/" name)} name]])]
           [:span "No Buffers :-( Use `M-x webmacs-mode' to publish buffers"])]))

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

(def server (atom nil))

;;; TODO: Move the & m args
(defn start-server [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "WEB_PORT" "3000"))
        noir-handler (server/gen-handler {:mode mode})]
    (reset! server (start-http-server
                    (wrap-ring-handler noir-handler)
                    {:port port :websocket true}))))

(defn stop-server []
  (@server)
  (reset! server nil))
