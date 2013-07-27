(ns isbnnetinclj2.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [stencil.core :as mus]
            [ring.adapter.jetty :as jetty]
            [isbnnetinclj2.store :as store]))

(defn front-page
  []
  (mus/render-file "frontpage" {:pageTitle "isbn.net.in"}))

(defn about-page
  []
  (mus/render-file "about" {:pageTitle "About isbn.net.in"}))

(defroutes app-routes
  (GET "/" [] (front-page))
  (GET "/about/" [] (about-page))
  (GET ["/:isbn" :isbn #"[\d-]+[xX]?"] [isbn] (store/book-page isbn))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (jetty/run-jetty app-routes
                   {:port (Integer. (or (System/getenv "PORT") 8080))
                    :join? false}))
