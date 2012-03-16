(ns webmacs.server
  (:use aleph.http)
  (:require [noir.server :as server]))

(server/load-views "src/webmacs/pages/")

(def server (atom nil))

;;; TODO: Move the & m args
(defn start-server [& m]
  (when @server
    (throw (IllegalStateException. "Webserver already running")))

  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "WEB_PORT" "3000"))
        noir-handler (server/gen-handler {:mode mode})]
    (reset! server (start-http-server
                    (wrap-ring-handler noir-handler)
                    {:port port :websocket true}))))

(defn stop-server []
  (when-let [s @server]
   (s))
  (reset! server nil))
