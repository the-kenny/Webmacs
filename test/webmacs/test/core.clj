(ns webmacs.test.core
  (:use webmacs.core
        midje.sweet))

(def encoded "RlVCQVI=")
(def decoded "FUBAR")

(def buffer "fubar.org")

(facts
  (parse-message (list 'insert buffer 1 3 encoded)) => [:insert buffer 0 2 decoded]
  (parse-message (list 'replace buffer 1 6 encoded)) => [:replace buffer 0 5 decoded]
  (parse-message (list 'delete buffer 1 6)) => [:delete buffer 0 5]
  (parse-message (list 'buffer-data "fubar.org" 5 encoded)) => [:buffer-data buffer (count decoded) decoded])
