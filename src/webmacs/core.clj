(ns webmacs.core
  (:require [webmacs.publishers :as publishers]
        [webmacs.server :as web])
  (:gen-class))

(defn bootstrap [web-port emacs-port]
  (publishers/start-listening emacs-port)
  (web/start-server web-port))

(defn shutdown []
  ;; TODO: Catch reader exception when closing listening socket
  (publishers/stop-listening)
  (web/stop-server))

(defn -main [& args]
  (bootstrap (Integer. (get (System/getenv) "WEB_PORT"   "3000"))
             (Integer. (get (System/getenv) "EMACS_PORT" "9881"))))
