(ns isbnnetinclj2.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [stencil.core :as mus]
            [ring.adapter.jetty :as jetty]))

(defn front-page-content
  []
  (mus/render-file "frontpage" {:title "isbn.net.in"}))

(defn about-page-content
  []
  (mus/render-file "about" {:title "About isbn.net.in"}))

(defroutes app-routes
  (GET "/" [] (front-page-content))
  (GET "/about/" [] (about-page-content))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (jetty/run-jetty app-routes
                   {:port (or (System/getenv "PORT") 8080) :join? false}))
