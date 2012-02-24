(ns webmacs.buffer)

(defrecord EmacsBuffer [filename contents])

(defn make-buffer [filename contents]
  (EmacsBuffer. filename contents))

(defn delete-region [buffer from to]
  (let [before (subs (:contents buffer) 0 from)
        after (subs (:contents buffer) to)]
    (assoc buffer :contents (.concat before after))))

(defn insert-data [buffer at data]
  (update-in buffer [:contents] #(let [before (subs % at)
                                       after (subs % at)]
                                   (str before data after))))
