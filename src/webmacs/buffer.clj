(ns webmacs.buffer)

(defrecord EmacsBuffer [filename contents])

(defn make-buffer [filename contents]
  (EmacsBuffer. filename contents))

(defn delete-region [buffer from to]
  (let [before (subs (:contents buffer) 0 from)
        after (subs (:contents buffer) to)]
    (assoc buffer :contents (.concat before after))))

(defn insert-data [buffer at data]
  (let [before (subs (:contents buffer) 0 at)
        after (subs (:contents buffer) at)]
    (assoc buffer :contents (str before data after))))

(defn replace-region [buffer from to data]
  (let [before (subs (:contents buffer) 0 from)
        after (subs (:contents buffer) to)]
   (assoc buffer :contents (str before data after))))
