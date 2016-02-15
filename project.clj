(defproject overdaw-fe "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [http-kit "2.1.19"]
                 [com.stuartsierra/component "0.3.0"]
                 [metosin/palikka "0.3.0"]
                 [metosin/kekkonen "0.1.0"]
                 [overtone "0.9.1"]
                 [leipzig "0.8.0"]
                 [compojure "1.4.0"]
                 [metosin/compojure-api "0.24.4"]

                 [reagent "0.5.1"]
                 [re-frame "0.6.0"]
                 [garden "1.3.0"]
                 [cljs-http "0.1.39"]
                 [cljs-ajax "0.5.3"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-garden "0.2.6"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css/compiled"]

  :main overdaw-fe.main

  :garden {:builds [{:id "screen"
                     :source-paths ["src/clj"]
                     :stylesheet overdaw-fe.css/screen
                     :compiler {:output-to "resources/public/css/compiled/screen.css"
                                :pretty-print? true}}]}

  :profiles
  {:dev {:plugins [[lein-figwheel "0.5.0-2"]
                   [lein-doo "0.1.6"]]
         :dependencies [[reloaded.repl "0.2.1"]
                        [figwheel "0.5.0-2"]
                        [binaryage/devtools "0.5.2"]
                        [org.clojars.stumitchell/clairvoyant "0.1.0-SNAPSHOT"]
                        [day8/re-frame-tracer "0.1.0-SNAPSHOT"]]
         :figwheel {:css-dirs ["resources/public/css"]}}

   :repl {:source-paths ["dev" "src/clj"]
          :resource-paths ^:replace ["resources" "target/figwheel"]
          :prep-tasks     ^:replace [["javac"] ["compile"]]}
   :uberjar {:aot :all}}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :figwheel {:on-jsload "overdaw-fe.core/mount-root"}
                        :compiler {:main overdaw-fe.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true}}

                       {:id "test"
                        :source-paths ["src/cljs" "test/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/test.js"
                                   :main overdaw-fe.runner
                                   :optimizations :none}}

                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:main overdaw-fe.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :optimizations :advanced
                                   :closure-defines {goog.DEBUG false}
                                   :pretty-print false}}]})
