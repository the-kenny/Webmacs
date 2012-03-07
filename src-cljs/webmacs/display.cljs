(ns webmacs.display
  (:require [webmacs.buffer :as buffer]
            [clojure.browser.dom :as dom]))

(defn make-line-count [n]
  (let [pre (dom/element :pre {:id "line-numbers"})]
    (doseq [n (range 1 (inc n))]
     (dom/append pre (dom/element :span {:id (str "L" n)} (str n)))
     (dom/append pre "\n"))
   pre))

(defn make-buffer-contents [lines]
  (let [pre (dom/element :pre {:id "buffer-contents"})]
    (doseq [line lines]
     (dom/append pre (dom/element :span line))
     (dom/append pre "\n"))
   pre))
