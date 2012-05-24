(ns webmacs.test.routes
  (:use webmacs.routes
        midje.sweet
        ring.mock.request
        [hiccup.core :only [html]]
        [webmacs.publishers :only [store-buffer! reset-publishers!]]
        [webmacs.buffer :only [make-buffer]]))

(def +name+ "pages.buffer.org")

(fact "/emacs/<buffer-name>"
  (app-routes (request :get (str "/emacs/" +name+)))
  => (contains {:body (contains (html [:title (str "Emacs: " +name+)]))}))

(future-fact "websocket on /sockets/<buffer-name>")


(fact "/ without buffers"
  (app-routes (request :get "/"))
  => (contains {:body (contains "No Buffers :-(")}))

(fact "/ with buffers"
  (app-routes (request :get "/"))
  => (contains {:body (contains +name+)})

  (against-background
    (before :facts (store-buffer! (make-buffer +name+ ...any...)))
    (after  :facts (reset-publishers!))))
