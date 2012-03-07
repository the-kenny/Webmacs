(ns webmacs.display
  (:require [webmacs.buffer :as buffer]
            [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [goog.array :as garray]))

(defn ^:private make-line-span [n]
  (dom/element :span {:id (str "L" n)} (str n)))

(defn make-line-count [n]
  (let [pre (dom/element :pre {:id "line-numbers"})]
    (doseq [n (range 1 (inc n))]
     (dom/append pre (make-line-span n) "\n"))
   pre))

(defn update-line-count [parent n]
  (let [old-n (count (garray/toArray (gdom/getChildren parent)))]
    (cond
      (< old-n n) (doseq [i (range old-n n)]
                    (dom/append parent (make-line-span i) "\n"))
      (> old-n n) (doseq [i (range n old-n)]
                    (let [el (dom/get-element (str "L" i))
                          sibling (.-nextSibling el)]
                     (gdom/removeNode el)
                     (gdom/removeNode sibling))))))

(defn make-buffer-contents [lines]
  (let [pre (dom/element :pre {:id "buffer-contents"})]
    (doseq [line lines]
     (dom/append pre (dom/element :span line))
     (dom/append pre "\n"))
   pre))
