(ns isbnnetinclj2.store
  (:require [taoensso.timbre :as log]
            [clojure.string :as string]
            [clojure.core.cache :as cache]
            [net.cgrand.enlive-html :as html]
            [clj-time.core :as time]
            [stencil.core :as mus]
            [isbnnetinclj2.utils :as utils]
            [isbnnetinclj2.mongo :as mongo]))


(defonce book-data-cache
  (atom (cache/ttl-cache-factory {} :ttl (* 60 60 24))))


(defonce book-in-progress-lock
  (atom {}))


(defn is-book-in-progress
  [isbn]
  (get @book-in-progress-lock isbn))


(defn set-book-in-progress
  [isbn]
  (swap! book-in-progress-lock assoc isbn true))


(defn done-book-in-progress
  [isbn]
  (swap! book-in-progress-lock dissoc isbn))


(defn pick-from-content
  [content path]
  (first
   (html/select
    content
    path)))


(defn parse-text-from-content
  [content path]
  (string/trim
   (pick-from-content content path)))


(defn parse-price-from-content
  [content path]
  (let [text (parse-text-from-content content path)]
    (if (empty? text)
      (Integer/MAX_VALUE)
      (try
        (Float/parseFloat
         (last
          (re-seq #"\d+(?:\.\d+)?"
                  (string/trim
                   (string/replace
                    (str text)
                    ","
                    "")))))
        (catch Exception x
          (do
            (log/error (str x))
            (str x)))))))


(defn flipkart-url
  [isbn]
  (format
   "http://www.flipkart.com/books/pr?q=%s&sid=bks&as=off&as-show=off&otracker=start&affid=INSwaroCom"
   isbn))


(defn pick-flipkart-values
  [content]
  (apply
   hash-map
   (map
    (comp html/text first :content)
    (html/select
     content
     [:table.fk-specs-type2 #{:td.specs-key :td.specs-value}]))))


(defn fetch-flipkart
  [isbn]
  (log/debug isbn "Fetching flipkart")
  (let [url (flipkart-url isbn)
        content (utils/fetch-page url)
        flipkart-values (pick-flipkart-values content)]
    {:url url
     :content content
     :price (parse-price-from-content
             content
             [:div.prices :span.fk-font-finalprice])
     :info {:title (parse-text-from-content
                    content
                    [:div.mprod-summary-title :h1 html/content])
            :imageSource "Flipkart"
            :imageLink url
            :image (get-in (pick-from-content
                            content
                            [:div#mprodimg-id :img])
                           [:attrs :data-src])
            :author (get flipkart-values "Author")
            :publisher (get flipkart-values "Publisher")
            :year (get flipkart-values "Publication Year")
            :binding (get flipkart-values "Binding")}}))


(defn infibeam-url
  [isbn]
  (format
   "http://www.infibeam.com/search.jsp?storeName=Books&query=%s"
   isbn))


(defn fetch-infibeam
  [isbn]
  (log/debug isbn  "Fetching infibeam")
  (let [url (infibeam-url isbn)
        content (utils/fetch-page url)]
    {:url url
     :content content
     :price (parse-price-from-content
             content
             [:span.infiPrice])}))


(defn homeshop18-url
  [isbn]
  (format
   "http://www.homeshop18.com/%s/search:%s/categoryid:10000/"
   isbn
   isbn))


(defn fetch-homeshop18
  [isbn]
  (log/debug isbn "Fetching homeshop18")
  (let [url (homeshop18-url isbn)
        content (utils/fetch-page url)]
    {:url url
     :content content
     :price (parse-price-from-content
             content
             [:span#hs18Price])}))


(defn snapdeal-url
  [isbn]
  (format
   "http://www.snapdeal.com/search?keyword=%s&santizedKeyword=&catId=&categoryId=364&suggested=false&vertical=p&noOfResults=20&clickSrc=go_header&lastKeyword=&prodCatId=&changeBackToAll=false&foundInAll=false&categoryIdSearched=&cityPageUrl=&url=&utmContent=&catalogID=&dealDetail="
   isbn))


(defn fetch-snapdeal
  [isbn]
  (log/debug isbn "Fetching snapdeal")
  (let [url (snapdeal-url isbn)
        content (utils/fetch-page url)]
    {:url url
     :content content
     :price (parse-price-from-content
             content
             [:div.product_price])}))


(defn amazon-india-url
  [isbn]
  (format
   "http://www.amazon.in/s/ref=nb_sb_noss?url=search-alias%%3Dstripbooks&amp;field-keywords=%s"
   isbn))


;;; TODO How to follow redirects?
(defn fetch-amazon-india
  [isbn]
  (log/debug isbn "Fetching amazon-india")
  (let [url (amazon-india-url isbn)
        content (utils/fetch-page url)]
    {:url url
     :content content
     :price (parse-price-from-content
             content
             [:span.bld.lrg.red])}))


(def stores
  {:flipkart {:url flipkart-url
              :parser fetch-flipkart}
   :infibeam {:url infibeam-url
              :parser fetch-infibeam}
   :homeshop18 {:url homeshop18-url
                :parser fetch-homeshop18}
   :snapdeal {:url snapdeal-url
              :parser fetch-snapdeal}})


(defn fetch-store
  [isbn store]
  (try
    (let [parser-function (get-in stores [(keyword store) :parser])
          details (parser-function isbn)]
      (log/debug isbn "Finished fetching" store)
      (swap! book-data-cache assoc-in [isbn :price store] (:price details))
      (when (:info details)
        (swap! book-data-cache assoc-in [isbn :info] (:info details)))
      ;; TODO Log :content into MongoDB
      true)
    (catch Exception x
      (do
        (log/error isbn (str x))
        (swap! book-data-cache assoc-in [isbn :price store] Integer/MAX_VALUE)
        false))))


;; Idea is this:
;; Final structure should look like:
;; book-data-cache = {
;;   isbn: {
;;     price: { infibeam: 0, flipkart: 0, ... },
;;     info: { title: "", author: "", publisher: "", ... }
;;   },
;;   ...
;; }
;;
;; Example:
;; {"9789382618348"
;;  {:info
;;   {:title "The Oath of the Vayuputras: Shiva Trilogy 3",
;;    :imageSource "Flipkart",
;;    :imageLink "http://www.flipkart.com/books/pr?q=9789382618348&sid=bks&as=off&as-show=off&otracker=start&affid=INSwaroCom",
;;    :image nil,
;;    :author "Amish Tripathi",
;;    :publisher "Westland",
;;    :year "2013",
;;    :binding "Paperback"},
;;   :price
;;   {"snapdeal" 210.0,
;;    "homeshop18" 200.0,
;;    "infibeam" 228.0,
;;    "flipkart" 179.0}}}
;;
;; And this would be sent to the template with slight modifications,
;; such as sorting by ascending price.
(defn fetch-all-stores
  [isbn]
  (if-not (is-book-in-progress isbn)
    (do
      (set-book-in-progress isbn)
      (log/debug isbn "Launching fetchers")
      (doseq [f (mapv
                 #(future (fetch-store isbn %)) (keys stores))]
        (deref f))
      (swap! book-data-cache assoc-in [isbn :when] (time/now))
      ;; There is a race condition here...
      (done-book-in-progress isbn)
      (log/debug isbn "Done")
      (let [data (get @book-data-cache isbn)
            data (assoc data :isbn isbn)]
        (mongo/create-new-entry data)
        data))
    (do
      (log/debug isbn "already in progress")
      nil)))


(defn book-data
  [isbn]
  (or (mongo/get-recent-entry isbn)
      (do
        (future (fetch-all-stores isbn))
        {:when (time/now)})))


(defn book-page
  [isbn]
  (let [data (book-data isbn)
        price (sort-by val (:price data))
        price (map #(apply hash-map
                           [:name (string/capitalize (name (key %)))
                            :amount (val %)
                            :url ((get-in stores [(key %) :url]) isbn)])
                   price)
        price (map #(if (= Integer/MAX_VALUE (:amount %))
                      (merge % {:amount "N/A"})
                      %)
                   price)]
    (mus/render-file
     "book"
     (merge
      data
      {:isbn isbn
       :pageTitle (or (get-in data [:info :title])
                      isbn)
       :price price}))))


(def ^:private sample-isbns
  ["9781449394707"
   "9789382618348"])
