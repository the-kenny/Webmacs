(ns webmacs.message
  (:import [org.apache.commons.codec.binary Base64]))

;;; TODO: Use a multimethod
(defn parse [term]
  (when (and (sequential? term) (>= (count term) 3))
    (let [op (first term)]
      (case op
        ;; TODO: :insert should only contain `at' and not `start' and `end'
        (insert replace) (let [[_ buffer start end data] term]
                           (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
                             [(keyword op) buffer (dec start) (dec end) decoded]))
        delete (let [[_ buffer start end] term] [:delete buffer (dec start) (dec end)])
        buffer-data (let [[_ buffer length data] term
                          decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
                      (assert (= (count decoded) length)) ;TODO: Better error checking
                      [:buffer-data buffer length decoded])))))
