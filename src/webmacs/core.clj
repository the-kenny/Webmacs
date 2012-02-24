(ns webmacs.core
  (:use [server.socket :as ss]
        [clojure.java.io :as io]
        [webmacs.buffer :as buffer]
        [webmacs.publishers :as publishers])
  (:import [org.apache.commons.codec.binary Base64]))

(defn parse-message [term]
  (when (and (sequential? term) (>= (count term) 3))
    (let [op (first term)]
      (case op
        (insert replace) (let [[_ buffer start end data] term]
                           (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
                             [(keyword op) buffer (dec start) (dec end) decoded]))
        delete (let [[_ buffer start end] term] [:delete buffer (dec start) (dec end)])
        buffer-data (let [[_ buffer length data] term
                          decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
                      (assert (= (count decoded) length)) ;TODO: Better error checking
                      [:buffer-data buffer length decoded])))))

(def ^:dynamic *buffer* nil)

(defn emacs-connection-loop [input output]
  (try
    (let [request (parse-message (read input))
          [op name & req-rest] request
          newbuffer (case op
                      :buffer-data (let [[length data] req-rest]
                                     (assoc *buffer*
                                       :filename name
                                       :contents data))
                      :insert (let [[start _ data] req-rest] (buffer/insert-data *buffer* start data))
                      :replace (let [[start end data] req-rest] (buffer/replace-region *buffer* start end data))
                      :delete (let [[start end] req-rest] (buffer/delete-region *buffer* start end)))]
      (println newbuffer)

      (publishers/buffer-changed! newbuffer request)
      (set! *buffer* newbuffer)

      ;; Trampoline instead of recur allows re-evaluation of functions from the repl
      (trampoline #'emacs-connection-loop input output))
    (catch java.net.SocketException e
      (println "Got SocketException:" (.getMessage e) "Exiting."))))

#_(def server-socket (ss/create-server 9881 (fn [is os]
                                              (let [ird (java.io.PushbackReader. (io/reader is))
                                                    owr (io/writer os)]
                                                (binding [*buffer* (buffer/make-buffer)]
                                                  (emacs-connection-loop ird owr))))))

#_(ss/close-server server-socket)
