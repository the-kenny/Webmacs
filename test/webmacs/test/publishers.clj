(ns webmacs.test.publishers
  (:use webmacs.publishers
        webmacs.buffer
        midje.sweet
        lamina.core))

(def +name+ "webmacs.test.publishers.org")

(against-background [(before :facts (reset-publishers!))
                     (after :facts (reset-publishers!))]
  (facts
    (buffer-names) => nil)

  (facts
    (let [buf (make-buffer +name+ "text")]
      (get-buffer +name+)     => nil
      (store-buffer! buf)     => any
      (buffer-names)          => sequential?
      (get-buffer +name+)     => buf
      (remove-buffer! +name+) => any
      (get-buffer +name+)     => nil))

  (fact (get-change-channel +name+) => nil)

  (fact
    (buffer-changed! [:insert +name+ ...any...]) => (throws AssertionError))

  (fact
    (store-buffer! (make-buffer +name+ "abc"))
    (buffer-changed! [:insert +name+ 0 "x"]) => (make-buffer +name+ "xabc")))
