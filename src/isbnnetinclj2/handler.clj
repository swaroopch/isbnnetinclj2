(ns isbnnetinclj2.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [stencil.core :as mus]))

(defn front-page-content
  []
  (mus/render-file "frontpage" {:title "isbn.net.in"}))

(defroutes app-routes
  (GET "/" [] (front-page-content))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
