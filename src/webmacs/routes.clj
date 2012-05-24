(ns webmacs.routes
  (:use compojure.core
        [aleph.http :only [wrap-aleph-handler]])
  (:require [webmacs.pages.root :as root]
            [webmacs.pages.buffer :as buffer]))

(defroutes app-routes
  (GET "/" [] (root/root-page))
  (GET "/emacs/:name" [name]
       (buffer/buffer-page name))
  (GET "/sockets/:name" [name]
       ;; TODO: Get rid  of the var
       (wrap-aleph-handler #(buffer/websocket-handler name %1 %2))))
