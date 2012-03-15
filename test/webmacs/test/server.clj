(ns webmacs.test.server
  (:use webmacs.server
        midje.sweet
        [hiccup.core :only [html]]
        [webmacs.publishers :only [store-buffer! reset-publishers!]]
        [webmacs.buffer :only [make-buffer]]))

(def +name+ "webmacs.test.server.org")
(def +url+ "http://localhost:3000")

(against-background [(before :contents (start-server))
                     (after  :contents (stop-server))]
  (fact "/ without buffers"
    (slurp +url+) => (contains "No Buffers :-("))

  (fact "/ with buffers"
    (slurp +url+) => (contains +name+)
    (against-background
      (before :facts (store-buffer! (make-buffer +name+ ...any...)))
      (after  :facts (reset-publishers!))))

  (fact "/emacs/<buffer-name>"
    (slurp (str +url+ "/emacs/" +name+)) => (contains (html [:title (str "Emacs: "
                                                                         +name+)])))

  (future-fact "websocket on /sockets/<buffer-name>"))
