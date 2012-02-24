(ns webmacs.core
  (:use [server.socket :as ss]
        [clojure.java.io :as io])
  (:import [org.apache.commons.codec.binary Base64]))

(defn parse-message [term]
  (when (and (sequential? term) (>= (count term) 3))
    (let [op (first term)]
      (case op
        (insert replace) (let [[_ start end data] term]
                           {:type (keyword op)
                                  :start start
                                  :end end
                                  :data data})
        delete (let [[_ start end] term] {:type :delete
                        :start start
                        :end end})
        buffer-data (let [[_ length name data] term
                          decoded (Base64/decodeBase64 data)]
                      (assert (= (count decoded) length))
                      {:type :buffer-data
                       :name name
                       :length length
                       :data (String. decoded)})))))

(defn emacs-connection-loop [input output]
  (println (str "" (parse-message (read input))))
  ;; Trampoline instead of recur allows re-evaluation of functions from the repl
  (trampoline #'emacs-connection-loop input output))

(def server-socket (ss/create-server 9881 (fn [is os]
                                            (let [ird (java.io.PushbackReader. (io/reader is))
                                                  owr (io/writer os)]
                                              (emacs-connection-loop ird owr)))))

(ss/close-server server-socket)
