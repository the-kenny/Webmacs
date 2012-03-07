(ns webmacs.buffer)

(defrecord EmacsBuffer [name contents])

(defn make-buffer
  ([] (make-buffer nil nil))
  ([name] (make-buffer name nil))
  ([name contents] (EmacsBuffer. name contents)))

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

(defmulti ^:private modification-dispatch #(first %2) :default :default)

(defmethod modification-dispatch :buffer-data [buffer [op name & req-rest]]
  (let [[length data] req-rest]
                     (assoc buffer
                       :name name
                       :contents data)))

(defmethod modification-dispatch :insert [buffer [op name & req-rest]]
  (let [[at data] req-rest] (insert-data buffer at data)))

(defmethod modification-dispatch :replace [buffer [op name & req-rest]]
  (let [[start end data] req-rest] (replace-region buffer start end data)))

(defmethod modification-dispatch :delete [buffer [op name & req-rest]]
  (let [[start end] req-rest] (delete-region buffer start end)))

(defmethod modification-dispatch :default [buffer request]
  (println "Unknown request: " request))

(defn apply-modification [buffer modification]
  (let [[op name & req-rest] modification]
    (modification-dispatch buffer modification)))
