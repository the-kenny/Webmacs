(ns webmacs.display
  (:require [webmacs.buffer :as buffer]
            [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [goog.array :as garray]
            [goog.style :as gstyle]))

(defn ^:private make-line-span [n]
  (dom/element :span {:id (str "L" n)
                      :rel (str "#L" n)} (str (inc n))))

(defn ^:private append-newline [parent elem]
  (dom/append parent elem "\n"))

(defn ^:private remove-element-and-newline [elem]
  (let [sibling (.-nextSibling elem)]
    (gdom/removeNode elem)
    (gdom/removeNode sibling)))

(defn update-line-count [parent n]
  (let [old-n (count (garray/toArray (gdom/getChildren parent)))]
    (cond
      (< old-n n) (doseq [i (range old-n n)]
                    (append-newline parent (make-line-span i)))
      (> old-n n) (doseq [i (range n old-n)]
                    (remove-element-and-newline (dom/get-element (str "L" i)))))))

(defn update-buffer-contents [parent lines]
  (let [new-n (count lines)
        els (garray/toArray (gdom/getChildren parent))
        old-n (count els)]
    (cond
      (< old-n new-n) (doseq [i (range old-n new-n)]
                        (append-newline parent (dom/element :span (get lines i)))g)
      (> old-n new-n) (doseq [i (range new-n old-n)]
                        (remove-element-and-newline (get els i))))

    (let [new-childs (garray/toArray (gdom/getChildren parent))]
      (doseq [n (range new-n),
              :let [el (get new-childs n),
                    text (get lines n)]]
        (when (not= (dom/get-value el) text)
          (dom/set-text el text))))))
