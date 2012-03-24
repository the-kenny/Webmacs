(ns webmacs.test.server
  (:use webmacs.server
        midje.sweet))

(def +port+ 3000)
(def +url+ (str "http://localhost:" +port+))

(facts "start-server"
  (slurp +url+)  => string?
  (start-server +port+) => (throws IllegalStateException)
  (against-background
    (before :contents (start-server +port+))
    (after  :contents (stop-server))))

(facts "stop-server"
  (stop-server) => any
  (slurp +url+) => throws)
