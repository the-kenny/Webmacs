(ns webmacs.core
  (:require [webmacs.buffer :as buffer]
            [webmacs.display :as display]
            [cljs.reader :as reader]
            [clojure.browser.dom :as dom]
            [clojure.string :as string]
            [clojure.browser.event :as event]
            [goog.dom :as gdom]
            [goog.events :as gevents]))

(def *socket* nil)
(def *buffer* nil)
(def *buffer-content-lines* nil)

(defn send-data [data]
  (binding [*print-meta* true]
    (.send *socket* (pr-str data))))

(def ^:private +conversion+ 1024)

(defn make-modeline [buffer]
  (let [name (:name buffer)
        mode (:mode buffer)
        size (.-length (or (:contents buffer) ""))
        size-str (cond
                   (< +conversion+ size) (str (Math/floor (/ size +conversion+)) "k")
                   (< (* +conversion+ +conversion+) size) (str (Math/floor (/ size (* +conversion+ +conversion+))) "M"))
        pos (Math/floor (* 100 (/ (.-y (gdom/getDocumentScroll)) (gdom/getDocumentHeight))))
        pos-str (cond
                  (= 0 (- (gdom/getDocumentHeight) (+ (.-y (gdom/getDocumentScroll))
                                                       (.-height (gdom/getViewportSize)))))
                  "Bot"

                  (pos? pos) (str pos "%")
                  true "Top")]
    (str "Buffer: " name "\t" pos-str " of " size-str "\t(" mode ")")))

(defn apply-narrow [buffer]
  (if-let [[beg end] (:narrow buffer)]
    (update-in buffer [:contents] subs beg end)
    buffer))
(defn update-modeline [parent buffer]
  (dom/set-text parent (make-modeline buffer)))

(defn display-buffer [buffer]
  (let [lines (string/split-lines (:contents buffer))]
   (display/update-line-count (dom/get-element :line-numbers) (count lines))
   (display/update-buffer-contents (dom/get-element :buffer-contents) lines)
   (update-modeline (dom/get-element :mode-line) buffer)))

;;; NOTE: Why does't event/listen work?
(gevents/listen (gdom/getWindow)
                gevents/EventType.SCROLL
                #(update-modeline (dom/get-element :mode-line) *buffer*))

;;; Connnection Stuff
(defn handle-socket-message [socket-event]
  (let [obj (reader/read-string (.-data socket-event))]
    (set! *buffer* (buffer/apply-modification *buffer* obj))
    ;; (set! *buffer-content-lines* (string/split-lines (:contents *buffer*)))

    (->> *buffer*
         (apply-narrow)
         (display-buffer))))

(defn handle-close [])
(defn handle-open [])

(defn ^:export open-socket [uri]
  (let [ws (if (.-MozWebSocket js/window) (js/MozWebSocket. uri) (js/WebSocket. uri))]
    (set! (.-onopen ws) (fn [_] (handle-open)))
    (set! (.-onclose ws) (fn [] (handle-close)))
    (set! (.-onmessage ws) #(handle-socket-message %))
    (set! *socket* ws)))
