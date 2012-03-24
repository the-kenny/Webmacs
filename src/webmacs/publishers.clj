(ns webmacs.publishers
  (:use [webmacs.buffer :as buffer]
        lamina.core
        [clojure.java.io :as io]
        [server.socket :as ss]
        [webmacs.message :as message]))

;;; TODO: Simplify using Broadcasts from netwars
(def buffer-channels (atom {}))          ;maps buffer-names to lamina channels
(def buffers (atom {}))

(defn buffer-names []
  (keys @buffers))

(defn get-buffer [name]
  (get @buffers name))

(defn store-buffer! [buffer]
  (swap! buffers assoc (:name buffer) buffer))

(defn remove-buffer! [name]
  (swap! buffers dissoc name))

(defn get-change-channel [buffer-name]
  (get @buffer-channels buffer-name))

(defn add-client! [buffer-name client-channel]
  (let [chan (get-change-channel buffer-name)]
    (when (or (nil? chan)
              (closed? chan))
      (swap! buffer-channels assoc buffer-name (channel))))

  (let [chan (get-change-channel buffer-name)
        buf (get-buffer buffer-name)]
    ;; First, send whole buffer
    ;; TODO: The message should be created in webmacs.message
    (enqueue client-channel [:buffer-data buffer-name (:mode buf) (count (:contents buf)) (:contents buf)])
    ;; Then start piping everything from `change-channel'
    (siphon chan client-channel)))

(defn remove-client! [client-channel] nil)

(defn buffer-changed! [change]
  (let [[_ name & _] change]
    (assert (get-buffer name) "Buffer must be initialized")

    (swap! buffers update-in [name] buffer/apply-modification change)

    (let [change-channel (get-change-channel name)
          newbuf (get-buffer name)]
      (when (and change-channel (not (closed? change-channel)))
        (enqueue change-channel change)
        change)
      newbuf)))

(defn reset-publishers! []
  ;; TODO: Send message to web-clients
  (reset! buffer-channels {})
  (reset! buffers {}))

;;; Connection Handling

(defn ^:private emacs-connection-loop [input output]
  (let [request (message/parse (read input))
        [op name & req-rest] request]
    (when (= op :buffer-data)
      (store-buffer! (make-buffer name)))

    (buffer-changed! request)
    (recur input output)))

(def listen-server (atom nil))

(defn start-listening [port]
  (reset! listen-server
          (ss/create-server port
                            (fn [is os]
                              (let [ird (java.io.PushbackReader. (io/reader is))
                                    owr (io/writer os)]
                                (try
                                  (emacs-connection-loop ird owr)
                                  (catch java.net.SocketException e
                                    (println "Got SocketException:" (.getMessage e) "Exiting."))))))))

(defn stop-listening []
  (when-let [s @listen-server]
    (ss/close-server s)
    (reset! listen-server nil)))
