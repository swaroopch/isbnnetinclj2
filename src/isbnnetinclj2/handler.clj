(ns isbnnetinclj2.handler
  (:require [taoensso.timbre :as log]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [stencil.core :as mus]
            [stencil.loader]
            [ring.adapter.jetty :as jetty]
            [clojure.string :as string]
            [clojure.core.cache]
            [isbnnetinclj2.mongo :as mongo]
            [isbnnetinclj2.store :as store]))


(defn front-page
  []
  (mus/render-file "frontpage" {:pageTitle "isbn.net.in"
                                :stores (sort (map
                                               (comp string/capitalize name)
                                               (keys store/stores)))}))


(defn about-page
  []
  (mus/render-file "about" {:pageTitle "About isbn.net.in"}))


(defroutes app-routes
  (GET "/" [] (front-page))
  (GET "/about/" [] (about-page))
  (GET ["/:isbn" :isbn #"[\d-]+[xX]?"] [isbn] (store/book-page isbn))
  (route/resources "/")
  (route/not-found "Not Found"))


(defn initialize
  []
  (mongo/init)
  (when-not (= (System/getenv "ENVIRONMENT") "production")
    ;; https://github.com/davidsantiago/stencil#manual-cache-management
    (stencil.loader/set-cache (clojure.core.cache/ttl-cache-factory {} :ttl 0))))


(defn destroy
  []
  (mongo/destroy))


(def app
  (handler/site app-routes))


(defn -main []
  (initialize)
  (jetty/run-jetty app-routes
                   {:port (Integer/parseInt (or (System/getenv "PORT")
                                                "8080"))
                    :join? false}))
