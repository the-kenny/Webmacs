(ns webmacs.test.core
  (:use webmacs.core
        midje.sweet))

(def encoded "RlVCQVI=")
(def decoded "FUBAR")

(facts
  (parse-message (list 'insert 1 3 encoded)) => [:insert 0 2 decoded]
  (parse-message (list 'replace 1 6 encoded)) => [:replace 0 5 decoded]
  (parse-message (list 'delete 1 6)) => [:delete 0 5]
  (parse-message (list 'buffer-data 5 "fubar.org" encoded)) => [:buffer-data "fubar.org" (count decoded) decoded])
