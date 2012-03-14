(ns webmacs.buffer)

(defrecord EmacsBuffer [name contents])

(defn make-buffer
  ([] (make-buffer nil nil))
  ([name] (make-buffer name nil))
  ([name contents] (EmacsBuffer. name contents)))

(defn ^:private update-narrow [narrow delta]
  (when-let [[beg end] narrow]
    [beg (+ end delta)]))

(defn delete-region [buffer from to]
  (let [before (subs (:contents buffer) 0 from)
        after (subs (:contents buffer) to)]
    (merge buffer
           {:contents (.concat before after)}
           (when-let [nn (update-narrow (:narrow buffer) (- from to))] {:narrow nn}))))

(defn insert-data [buffer at data]
  (let [before (subs (:contents buffer) 0 at)
        after (subs (:contents buffer) at)]
    (merge buffer
           {:contents (str before data after)}
           (when-let [nn (update-narrow (:narrow buffer) (count data))] {:narrow nn}))))

(defn replace-region [buffer from to data]
  (let [before (subs (:contents buffer) 0 from)
        after (subs (:contents buffer) to)]
    (merge buffer
           {:contents (str before data after)}
           (when-let [nn (update-narrow (:narrow buffer) (- (count data) (- to from)))] {:narrow nn}))))

(defmulti ^:private modification-dispatch #(first %2) :default :default)

(defmethod modification-dispatch :buffer-data [buffer [op name & req-rest]]
  (let [[mode length data] req-rest]
    (-> buffer
        (assoc
            :name name
            :mode mode
            :contents data)
        (dissoc :narrow))))

(defmethod modification-dispatch :insert [buffer [op name & req-rest]]
  (let [[at data] req-rest] (insert-data buffer at data)))

(defmethod modification-dispatch :replace [buffer [op name & req-rest]]
  (let [[start end data] req-rest] (replace-region buffer start end data)))

(defmethod modification-dispatch :delete [buffer [op name & req-rest]]
  (let [[start end] req-rest] (delete-region buffer start end)))

(defmethod modification-dispatch :narrow [buffer [op name point-min point-max]]
  (assoc buffer :narrow [point-min point-max]))

(defmethod modification-dispatch :widen [buffer [op name _ _]]
  (dissoc buffer :narrow))

;;; TODO: Fix println in clojurescript
(defmethod modification-dispatch :default [buffer request]
  (println "Unknown request: " request))

(defn apply-modification [buffer modification]
  (let [[op name & req-rest] modification]
    (modification-dispatch buffer modification)))
