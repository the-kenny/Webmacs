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

(defpage "/emacs/:name" {:keys [name]}
  (html5 [:head
          [:title "Emacs: " name]
          (include-js "/js/main.js")]
         [:body
          [:pre {:id "buffer-contents"}]
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

(defn start-server [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "3000"))
        noir-handler (server/gen-handler {:mode mode})]
    (reset! server (start-http-server
                    (wrap-ring-handler noir-handler)
                    {:port port :websocket true}))))

(defn stop-server []
  (@server)
  (reset! server nil))
