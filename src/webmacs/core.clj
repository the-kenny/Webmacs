(ns webmacs.core
  (:use [webmacs.publishers :as publishers]
        [webmacs.server :as web])
  (:gen-class))


(defn -main [& args]
  (apply web/start-server args)
  (publishers/listen 9881))
