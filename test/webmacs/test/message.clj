(ns webmacs.test.message
  (:use webmacs.message
        midje.sweet))

(def encoded "RlVCQVI=")
(def decoded "FUBAR")

(def buffer "fubar.org")

(facts
  (parse (list 'insert buffer 1 encoded)) => [:insert buffer 0 decoded]
  (parse (list 'replace buffer 1 6 encoded)) => [:replace buffer 0 5 decoded]
  (parse (list 'delete buffer 1 6)) => [:delete buffer 0 5]
  (parse (list 'buffer-data "fubar.org" 5 encoded)) => [:buffer-data buffer (count decoded) decoded])
