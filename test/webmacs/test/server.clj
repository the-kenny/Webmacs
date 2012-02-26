(ns webmacs.test.server
  (:use webmacs.server
        midje.sweet
        [hiccup.core :only [html]]))

(background (before :facts (start-server))
            (after :facts (stop-server)))

(def +name+ "foo.org")

(fact "/emacs/<buffer-name>"
  (slurp (str "http://localhost:3000/emacs/" +name+)) => (contains (html [:title (str "Emacs: " +name+)])))

(future-fact "websocket on /sockets/<buffer-name>")
