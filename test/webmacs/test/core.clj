(ns webmacs.test.core
  (:use webmacs.core
        midje.sweet))

(future-fact "-main")
(future-fact "WEB_PORT")
(future-fact "EMACS_PORT")
