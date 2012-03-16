(ns webmacs.test.pages.buffer
  (:use webmacs.pages.buffer
        midje.sweet
        noir.util.test
        [hiccup.core :only [html]]
        [webmacs.publishers :only [store-buffer! reset-publishers!]]
        [webmacs.buffer :only [make-buffer]]))

(def +name+ "pages.buffer.org")

(fact "/emacs/<buffer-name>"
  (send-request (str "/emacs/" +name+)) => (contains {:body (contains (html [:title (str "Emacs: "
                                                                                         +name+)]))}))

(future-fact "websocket on /sockets/<buffer-name>")
