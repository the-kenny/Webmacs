(ns webmacs.server
  (:use aleph.http)
  (:require [webmacs.routes :as routes]))

(def server (atom nil))

;;; TODO: Move the & m args
(defn start-server [port]
  (when @server
    (throw (IllegalStateException. "Webserver already running")))

  (reset! server (start-http-server
                  (wrap-ring-handler routes/app-routes)
                  {:port port :websocket true})))

(defn stop-server []
  (when-let [s @server]
    (s)
    (reset! server nil)))
