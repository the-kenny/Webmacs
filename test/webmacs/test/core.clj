(ns webmacs.test.core
  (:use webmacs.core
        midje.sweet))

(def encoded "RlVCQVI=")
(def decoded "FUBAR")

(facts
  (parse-message (list 'insert 0 2 encoded)) => {:type :insert
                                                 :start 0
                                                 :end 2
                                                 :data decoded}
  (parse-message (list 'replace 0 5 encoded)) => {:type :replace
                                                  :start 0
                                                  :end 5
                                                  :data decoded}
  (parse-message (list 'delete 0 5)) => {:type :delete
                                         :start 0
                                         :end 5}
  (parse-message (list 'buffer-data 5 "fubar.org" encoded))
  => {:type :buffer-data
      :name "fubar.org"
      :length (count decoded)
      :data decoded})
