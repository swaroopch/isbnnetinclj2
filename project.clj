(defproject isbnnetinclj "0.8"
  :description "A quick way to find the online prices for a book in India"
  :url "http://isbn.net.in"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [com.taoensso/timbre "2.4.1"]
                 [com.taoensso/faraday "0.10.2"]
                 [com.taoensso/carmine "2.0.0"]
                 [stencil "0.3.2"]
                 [enlive "1.1.1"]
                 [cheshire "5.2.0"]
                 [clj-time "0.5.1"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler isbnnetinclj.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
