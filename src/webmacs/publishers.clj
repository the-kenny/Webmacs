(ns webmacs.publishers
  (:use webmacs.buffer
        lamina.core))

;;; TODO: Simplify using Broadcasts from netwars
(def buffer-channels (atom {}))          ;maps buffer-names to lamina channels
(def buffers (atom {}))

(defn get-change-channel [buffer-name]
  (get @buffer-channels buffer-name))

(defn add-client! [buffer-name client-channel]
  (let [chan (get-change-channel buffer-name)]
    (when (or (nil? chan)
              (closed? chan))
      (swap! buffer-channels assoc buffer-name (channel))))

  (let [chan (get-change-channel buffer-name)
        buf (get @buffers buffer-name)]
    ;; First, send whole buffer
    (enqueue client-channel [:buffer-data buffer-name (count (:contents buf)) (:contents buf)])
    ;; Then start piping everything from `change-channel'
    (siphon chan client-channel)))

(defn remove-client! [client-channel] nil)

(defn buffer-changed! [buffer change]
  (swap! buffers assoc (:filename buffer) buffer)

  (let [change-channel (get-change-channel (:filename buffer))]
    (when (and change-channel (not (closed? change-channel)))
      (enqueue change-channel change)
      change)))

(defn reset-publishers! []
  ;; TODO: Send message to web-clients
  (reset! buffer-channels {})
  (reset! buffers {}))
