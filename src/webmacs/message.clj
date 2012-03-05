(ns webmacs.message
  (:import [org.apache.commons.codec.binary Base64]))

(defmulti parse first)

;; TODO: :insert should only contain `at' and not `start' and `end'
(defmethod parse 'insert [[op buffer start end data]]
  (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
    [(keyword op) buffer (dec start) (dec end) decoded]))

(defmethod parse 'replace [[op buffer start end data]]
  (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
    [(keyword op) buffer (dec start) (dec end) decoded]))

(defmethod parse 'delete [[_ buffer start end]]
  [:delete buffer (dec start) (dec end)])

(defmethod parse 'buffer-data [[_ buffer length data]]
  (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
    (assert (= (count decoded) length)) ;TODO: Better error checking
    [:buffer-data buffer length decoded]))
