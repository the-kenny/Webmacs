(ns webmacs.test.core
  (:use webmacs.core
        midje.sweet))

(facts
  (parse-message '(insert 0 2 "aa")) => {:type :insert
                                         :start 0
                                         :end 2
                                         :data "aa"}
  (parse-message '(replace 0 5 "aa")) => {:type :replace
                                          :start 0
                                          :end 5
                                          :data "aa"}
  (parse-message '(delete 0 5)) => {:type :delete
                                    :start 0
                                    :end 5}
  (parse-message '(buffer-data 5 "fubar.org" "RlVCQVI="))
  => {:type :buffer-data
      :name "fubar.org"
      :length 5
      :data "FUBAR"})
