(ns webmacs.core
  (:use [server.socket :as ss]
        [clojure.java.io :as io]
        [webmacs.buffer :as buffer]
        [webmacs.publishers :as publishers]
        [webmacs.server :as web])
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
  (let [request (parse-message (read input))
        [op name & req-rest] request
        newbuffer (buffer/apply-modification *buffer* request)]

    (publishers/buffer-changed! newbuffer request)
    (set! *buffer* newbuffer)

    (recur input output)))

(defn open-server-socket [port]
  (ss/create-server port
                    (fn [is os]
                      (let [ird (java.io.PushbackReader. (io/reader is))
                            owr (io/writer os)]
                        (try
                          (binding [*buffer* (buffer/make-buffer)]
                            (emacs-connection-loop ird owr))
                          (catch java.net.SocketException e
                            (println "Got SocketException:" (.getMessage e) "Exiting.")))))))

#_(def server-socket (open-server-socket 9881))
#_(ss/close-server server-socket)

(defn -main [& args]
  (apply web/start-server args)
  (open-server-socket 9881))
