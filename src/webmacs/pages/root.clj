(ns webmacs.pages.root
  (:use hiccup.core
        hiccup.page-helpers)
  (:require [webmacs.publishers :as publishers]))

(defn root-page []
  (html5 [:head
          [:title "Webmacs"]]
         [:body
          (if-let [buffers (seq (publishers/buffer-names))]
            [:ul
             (for [name buffers]
               [:li [:a {:href (str "/emacs/" name)} name]])]
            [:span "No Buffers :-( Use `M-x webmacs-mode' to publish buffers"])]))
