(ns isbnnetinclj2.store
  (:require [taoensso.timbre :as log]
            [isbnnetinclj2.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as string]
            [stencil.core :as mus]))


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
        (int
         (* 100
            (Float/parseFloat
             (last
              (re-seq #"\d+(?:\.\d+)?"
                      (string/trim
                       (string/replace
                        (str text)
                        ","
                        "")))))))
        (catch Exception x
          (do
            (log/error (str x))
            (str x)))))))


(defn flipkart-url
  [isbn]
  (format
   "http://www.flipkart.com/books/pr?q=%s&sid=bks&as=off&as-show=off&otracker=start&affid=INSwaroCom"
   isbn))


;;; TODO Convert keys to keywords
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
  (log/debug (format "Fetching flipkart for %s" isbn))
  (let [url (flipkart-url isbn)
        content (utils/fetch-page url)
        flipkart-values (pick-flipkart-values content)]
    {:isbn isbn
     :title (parse-text-from-content
             content
             [:div.mprod-summary-title :h1 html/content])
     :imageSource "Flipkart"
     :imageLink url
     :image (get-in (pick-from-content
                     content
                     [:div#mprodimg-id :img])
                    [:attrs :data-src])
     :priceFlipkart (parse-price-from-content
                      content
                      [:div.prices :span.fk-font-finalprice])
     :author (get flipkart-values "Author")
     :publisher (get flipkart-values "Publisher")
     :year (get flipkart-values "Publication Year")
     :binding (get flipkart-values "Binding")}))


(defn infibeam-url
  [isbn]
  (format
   "http://www.infibeam.com/search.jsp?storeName=Books&query=%s"
   isbn))


(defn fetch-infibeam
  [isbn]
  (log/debug (format "Fetching infibeam for %s" isbn))
  (let [url (infibeam-url isbn)
        content (utils/fetch-page url)]
    {:priceInfibeam (parse-price-from-content
                     content
                     [:span.infiPrice])}))


(defn book-page
  [isbn]
  (let [flipkart-details (fetch-flipkart isbn)
        infibeam-details (fetch-infibeam isbn)]
    (mus/render-file "book"
                     (reduce
                      merge
                      {:isbn isbn
                       :pageTitle (:title flipkart-details)}
                      [flipkart-details
                       infibeam-details]))))
