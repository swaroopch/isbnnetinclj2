(ns isbnnetinclj2.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.joda-time]
            [monger.json]
            [clj-time.core :as time]
            [isbnnetinclj2.utils :as utils]))


(defn uri
  []
  (or (System/getenv "MONGOLAB_URI")    ; https://addons.heroku.com/mongolab
      "mongodb://127.0.0.1:27017/isbnnetinclj2"))


(defn init
  []
  (mg/connect-via-uri! (uri)))


(defn destroy
  []
  (mg/disconnect!))


(def data-collection "data")


(defn get-recent-entry
  [isbn]
  (first
   (mq/with-collection
     data-collection
     (mq/find {:isbn isbn :when {"$gt" (utils/twenty-four-hours-ago)}})
     (mq/sort {:when -1})
     (mq/limit 1))))


(defn create-new-entry
  [data]
  (mc/insert-and-return
   data-collection
   data))


(def content-collection "content")


(defn save-content
  [isbn store content]
  (mc/insert-and-return
   content-collection
   {:isbn isbn
    :when (time/now)
    :store store
    :content content}))
