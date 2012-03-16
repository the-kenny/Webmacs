(ns webmacs.test.pages.root
  (:use webmacs.pages.root
        midje.sweet
        noir.util.test
        [hiccup.core :only [html]]
        [webmacs.publishers :only [store-buffer! reset-publishers!]]
        [webmacs.buffer :only [make-buffer]]))

(def +name+ "pages.root.org")


(fact "/ without buffers"
  (send-request "/") => (contains {:body (contains "No Buffers :-(")}))

(fact "/ with buffers"
  (send-request "/") => (contains {:body (contains +name+)})
  (against-background
    (before :facts (store-buffer! (make-buffer +name+ ...any...)))
    (after  :facts (reset-publishers!))))
