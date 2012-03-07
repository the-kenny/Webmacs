(ns webmacs.core
  (:require [webmacs.buffer :as buffer]
            [webmacs.display :as display]
            [cljs.reader :as reader]
            [clojure.browser.dom :as dom]
            [clojure.string :as string]))

(def *socket* nil)
(def *buffer* nil)
(def *buffer-content-lines* nil)

(defn send-data [data]
  (binding [*print-meta* true]
    (.send *socket* (pr-str data))))

;;; Connnection Stuff
(defn handle-socket-message [socket-event]
  (let [obj (reader/read-string (.-data socket-event))]
    (set! *buffer* (buffer/apply-modification *buffer* obj))
    (set! *buffer-content-lines* (string/split-lines (:contents *buffer*)))

    (dom/set-text (dom/get-element :mode-line) (str "Buffer: " (:name *buffer*)))

    (dom/replace-node (dom/get-element :line-numbers) (display/make-line-count (count *buffer-content-lines*)))
    (dom/replace-node (dom/get-element :buffer-contents) (display/make-buffer-contents *buffer-content-lines*))))

(defn handle-close [])
(defn handle-open [])


(defn ^:export open-socket [uri]
  (let [ws (js/WebSocket. uri)]
    (set! (.-onopen ws) (fn [_] (handle-open)))
    (set! (.-onclose ws) (fn [] (handle-close)))
    (set! (.-onmessage ws) #(handle-socket-message %))
    (set! *socket* ws)))
