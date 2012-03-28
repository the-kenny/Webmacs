(ns webmacs.test.core
  (:use webmacs.core
        midje.sweet))

(defn ^:private test-web-socket [port]
  (slurp (str "http://localhost:" port)) => any)

(defn ^:private test-emacs-socket [port]
  (doto (java.net.Socket. "localhost" port) .close) => any)

(fact "-main opens port 3000 and 9881 by default"
  (test-web-socket 3000)
  (test-emacs-socket 9881)
  (against-background
      (before :contents ((var webmacs.core/-main))) ;Hack
      (after  :contents (webmacs.core/shutdown))))

(future-fact "WEB_PORT")
(future-fact "EMACS_PORT")
