(defproject secrets "1.0.0"
  :description "Exchanges Secrets for the Album Tarred and Feathered By Agatha Frisky"
  :url "secrets.audio"
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cljs-ajax "0.5.8"]
                 [com.taoensso/timbre "4.10.0"]
                 [compojure "1.5.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/java.jdbc "0.7.0-alpha3"]
                 [org.clojure/clojurescript "1.9.495"
                  :exclusions [org.apache.ant/ant]]
                 [org.postgresql/postgresql "42.0.0"]
                 [reagent "0.6.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-core "1.6.0-RC1"]
                 [ring/ring-defaults "0.2.3"]]
  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-ring "0.9.7"]]
  :ring {:handler secrets.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}}
  :cljsbuild
  {:builds [
            {:id "dev"
             :source-paths ["src/cljs"]
             :compiler {:output-to "resources/public/index.js"
                        :optimizations :whitespace
                        :pretty-print true}}
            {:id "prod"
             :source-paths ["src/cljs"]
             :compiler {:output-to "resources/public/index.js"
                        :optimizations :advanced
                        :pretty-print false}}]})

