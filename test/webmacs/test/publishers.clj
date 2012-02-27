(ns webmacs.test.publishers
  (:use webmacs.publishers
        webmacs.buffer
        midje.sweet
        lamina.core))

(def +name+ "foo.org")

(against-background [(before :facts (reset-publishers!))]
  (facts
    (let [buf (make-buffer +name+ "text")]
      (get-buffer +name+)     => nil
      (store-buffer! buf)     => any
      (get-buffer +name+)     => buf
      (remove-buffer! +name+) => any
      (get-buffer +name+)     => nil))

  (fact (get-change-channel +name+) => nil)

  (fact
    (buffer-changed! (make-buffer +name+) ...any...) => nil)

  (fact
    (add-client! +name+ (channel)) => true
    (buffer-changed! (make-buffer +name+) ...any...) => ...any...))

(let [cchan (permanent-channel)
      change-atom (atom nil)
      buf (make-buffer +name+ "abc")]
  (reset-publishers!)
  (receive-all cchan #(reset! change-atom %))

  ;; First, create a buffer
  (facts
    (buffer-changed! buf [:buffer-data (:filename buf) (count (:contents buf)) (:contents buf)])
    @buffers => (contains {+name+ any}))

  (fact
    (add-client! +name+ cchan)
    @change-atom => [:buffer-data +name+ 3 "abc"])

  (fact
    (buffer-changed! buf [:replace +name+ 0 1 "b"])
    @change-atom => [:replace +name+ 0 1 "b"])

  (future-fact "remove-client!"))
