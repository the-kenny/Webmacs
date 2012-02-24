(ns webmacs.core
  (:use [server.socket :as ss]
        [clojure.java.io :as io]
        [webmacs.buffer :as buffer])
  (:import [org.apache.commons.codec.binary Base64]))

(defn parse-message [term]
  (when (and (sequential? term) (>= (count term) 3))
    (let [op (first term)]
      (case op
        (insert replace) (let [[_ start end data] term]
                           (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
                             {:type (keyword op)
                              :start (dec start)
                              :end (dec end)
                              :data decoded}))
        delete (let [[_ start end] term] {:type :delete
                                          :start (dec start)
                                          :end (dec end)})
        buffer-data (let [[_ length name data] term
                          decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
                      (assert (= (count decoded) length))
                      {:type :buffer-data
                       :name name
                       :length length
                       :data decoded})))))

(def ^:dynamic *buffer* nil)

(defn emacs-connection-loop [input output]
  (let [request (parse-message (read input))
        _ (println request)
        newbuffer (case (:type request)
                    :buffer-data (assoc *buffer*
                                   :filename (:name request)
                                   :contents (:data request))
                    :insert (buffer/insert-data *buffer* (:start request) (:data request))
                    :replace (buffer/replace-region *buffer* (:start request) (:end request) (:data request))
                    :delete (buffer/delete-region *buffer* (:start request) (:end request)))]
    (println newbuffer)

    (set! *buffer* newbuffer)

    ;; Trampoline instead of recur allows re-evaluation of functions from the repl
    (trampoline #'emacs-connection-loop input output)))

#_(def server-socket (ss/create-server 9881 (fn [is os]
                                              (let [ird (java.io.PushbackReader. (io/reader is))
                                                    owr (io/writer os)]
                                                (binding [*buffer* (buffer/make-buffer)]
                                                  (emacs-connection-loop ird owr))))))

#_(ss/close-server server-socket)
