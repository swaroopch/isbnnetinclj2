(defproject isbnnetinclj2 "0.8"
  :description "A quick way to find the online prices for a book in India"
  :url "http://isbn.net.in"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"] ; https://github.com/clojure/clojure
                 [compojure "1.1.5"]    ; https://github.com/weavejester/compojure
                 [com.taoensso/timbre "2.4.1"] ; https://github.com/ptaoussanis/timbre
                 [com.taoensso/carmine "2.0.0"] ; https://github.com/ptaoussanis/carmine
                 [stencil "0.3.2"]      ; https://github.com/davidsantiago/stencil
                 [enlive "1.1.1"]       ; https://github.com/cgrand/enlive
                 [cheshire "5.2.0"]     ; https://github.com/dakrone/cheshire
                 [clj-time "0.5.1"]     ; https://github.com/clj-time/clj-time
                 ]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler isbnnetinclj2.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
