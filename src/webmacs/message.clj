(ns webmacs.message
  (:import [org.apache.commons.codec.binary Base64]))

(defmulti parse first)

(defmethod parse 'insert [[_ buffer at data]]
  (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
    [:insert buffer (dec at) decoded]))

(defmethod parse 'replace [[_ buffer start end data]]
  (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
    [:replace buffer (dec start) (dec end) decoded]))

(defmethod parse 'delete [[_ buffer start end]]
  [:delete buffer (dec start) (dec end)])

(defmethod parse 'buffer-data [[_ buffer length data]]
  (let [decoded (String. (Base64/decodeBase64 ^String data) "utf-8")]
    (assert (= (count decoded) length)) ;TODO: Better error checking
    [:buffer-data buffer length decoded]))
