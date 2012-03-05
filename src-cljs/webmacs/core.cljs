(ns webmacs.core
  (:require [webmacs.buffer :as buffer]
            [cljs.reader :as reader]
            [goog.dom :as dom]))

(def ^:dynamic *socket* nil)
(def ^:dynamic *buffer* nil)


(defn send-data [data]
  (binding [*print-meta* true]
    (.send *socket* (pr-str data))))

;;; Connnection Stuff
(defn handle-socket-message [socket-event]
  (let [obj (reader/read-string (.-data socket-event))]
    (.log js/console (pr-str obj))
    (set! *buffer* (buffer/apply-modification *buffer* obj))
    (dom/setTextContent (dom/getElement "mode-line") (str "Buffer: " (:name *buffer*)))
    ;; TODO: Buffer changed
    (dom/setTextContent (dom/getElement "buffer-contents") (:contents *buffer*))))

(defn handle-close [])
(defn handle-open [])


(defn ^:export open-socket [uri]
  (let [ws (js/WebSocket. uri)]
    (set! (.-onopen ws) (fn [_] (handle-open)))
    (set! (.-onclose ws) (fn [] (handle-close)))
    (set! (.-onmessage ws) #(handle-socket-message %))
    (set! *socket* ws)))
