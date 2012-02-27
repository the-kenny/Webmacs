(defproject webmacs "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [server-socket "1.0.0"]
                 [commons-codec "1.5" :exclusions [commons-logging]] ;For base64
                 [noir "1.2.2"]
                 [noir-async "0.1.2"]]
  :dev-dependencies [[midje "1.3.1"]]
  :plugins [[lein-cljsbuild "0.1.0"]]
  :cljsbuild {:crossovers [webmacs.buffer],
              :builds [{:source-path "src-cljs/",
                        :compiler {:output-to "resources/public/js/main.js",
                                   :optimizations :whitespace,
                                   :pretty-print true}}]}
  :main webmacs.core)
