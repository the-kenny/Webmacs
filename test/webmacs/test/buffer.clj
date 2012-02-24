(ns webmacs.test.buffer
  (:use webmacs.buffer
        midje.sweet)
  (:import [webmacs.buffer EmacsBuffer]))

(fact (delete-region (make-buffer ...any... "aaabbbccc") 0 3) => (EmacsBuffer. ...any... "bbbccc"))

(fact (insert-data (make-buffer ...any... "aaaccc") 3 "bbb") => (EmacsBuffer. ...any... "aaabbbccc"))

(fact (replace-region (make-buffer ...any... "dafbbbccc") 0 3 "aaa") => (EmacsBuffer. ...any... "aaabbbccc"))
