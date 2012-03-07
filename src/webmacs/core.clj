(ns webmacs.core
  (:use [webmacs.publishers :as publishers]
        [webmacs.server :as web])
  (:gen-class))


(defn -main [& args]
  (apply web/start-server args)
  (publishers/listen (Integer. (get (System/getenv) "EMACS_PORT" "9881"))))
