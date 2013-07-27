(defproject isbnnetinclj2 "0.8"
  :description "A quick way to find the online prices for a book in India"
  :url "http://isbn.net.in"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"] ; https://github.com/clojure/clojure
                 [org.clojure/core.cache "0.6.3"] ; http://clojure.github.io/core.cache/
                 [compojure "1.1.5"]    ; https://github.com/weavejester/compojure
                 [ring/ring-jetty-adapter "1.2.0"] ; https://github.com/ring-clojure/ring
                 [com.taoensso/timbre "2.4.1"] ; https://github.com/ptaoussanis/timbre
                 [com.novemberain/monger "1.6.0"] ; http://clojuremongodb.info
                 [stencil "0.3.2"]      ; https://github.com/davidsantiago/stencil
                 [enlive "1.1.1"]       ; https://github.com/cgrand/enlive
                 [cheshire "5.2.0"]     ; https://github.com/dakrone/cheshire
                 [clj-time "0.5.1"]     ; https://github.com/clj-time/clj-time
                 ]
  :plugins [[lein-ring "0.8.5"]]
  :main isbnnetinclj2.handler
  :ring {:handler isbnnetinclj2.handler/app
         :init isbnnetinclj2.handler/initialize
         :destroy isbnnetinclj2.handler/destroy}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
