(ns webmacs.display
  (:require [webmacs.buffer :as buffer]
            [clojure.string :as string]
            [clojure.browser.dom :as dom]))

(defn make-line-count [text]
  (let [pre (dom/element :pre {:id "line-numbers"})]
   (doseq [n (range 1 (inc (count (string/split-lines text))))]
     (dom/append pre (dom/element :span {:id (str "L" n)} (str n)))
     (dom/append pre "\n"))
   pre))

(defn make-buffer-contents [text]
  (let [pre (dom/element :pre {:id "buffer-contents"})]
    (doseq [line (string/split-lines text)]
     (dom/append pre (dom/element :span line))
     (dom/append pre "\n"))
   pre))
