(ns isbnnetinclj2.store
  (:require [stencil.core :as mus]))

(defn book-page
  [isbn]
  (mus/render-file "book"
                   {:isbn isbn
                    :title isbn}))
