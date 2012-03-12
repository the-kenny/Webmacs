(defproject webmacs "0.0.1-SNAPSHOT"
  :description "View your Emacs buffers in a browser. Live."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [server-socket "1.0.0"]
                 [commons-codec "1.5"]
                 [noir "1.2.2"]
                 [noir-async "0.1.2"]]
  :cljsbuild {:crossovers [webmacs.buffer],
              :builds [{:source-path "src-cljs/",
                        :compiler {:output-to "resources/public/js/main.js",
                                   :optimizations :whitespace,
                                   :pretty-print true}}]}
  :profiles {:dev {:dependencies [[midje "1.3.1"]]}}
  :main webmacs.core
  :min-lein-version "2.0.0"
  :plugins [[lein-cljsbuild "0.1.2"]])
