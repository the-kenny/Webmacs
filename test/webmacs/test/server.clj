(ns webmacs.test.server
  (:use webmacs.server
        midje.sweet))

(def +url+ "http://localhost:3000/")

(facts "start-server"
  (slurp +url+)  => string?
  (start-server) => (throws IllegalStateException)
  (against-background
    (before :contents (start-server))
    (after  :contents (stop-server))))

(facts "stop-server"
  (stop-server) => any
  (slurp +url+) => throws)
