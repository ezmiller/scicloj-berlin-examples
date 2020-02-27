(ns panthera-demo
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [panthera.panthera :as pt]
            [panthera.pandas.utils :as pdutils]
            [clojure.pprint :refer [pprint print-table]]))

(py/initialize!)
(require-python '[pandas :as pd])

(defn data-source [filename]
  (apply str "data/berlin-airbnb-data/" filename))

;; Getting our data
;; * reviews-summary: a bunch of reviews
;; * listings-summary: a bunch of listings
;;
(def reviews-summary
  (pd/read_csv (data-source "reviews_summary.csv")))
(def listings-summary
  (pd/read_csv (data-source "listings_summary.csv")))


;; We want to merge this together so that we have informtaion about the listings
;; alongside the reviews data. We'll define a function.
;;
(defn merge-listings-and-reviews [ds1 ds2]
  (-> (pt/merge-ordered ds1 ds2 {:left-on :listing_id :right-on :id :how :left})
      (pt/rename {:columns {:id_x :id :neighbourhood_group_cleansed :neighbourhood_group}})
      (pt/drop-cols :id_y)))


;; Notice how beutiful this is! A simple pipe. This function is also super readable.

;; Quickly: What's panthera doing here? It's using libpython-clj
;; Let's look:
;;   https://github.com/alanmarazzi/panthera/blob/5cc62150519e86550015f0e0355c7270d3649c11/src/panthera/pandas/generics.clj#L1204

;; Now we'll actually call our function.
(def listings-and-reviews
  (merge-listings-and-reviews
    reviews-summary
    (pt/subset-cols listings-summary
                    :id :host_id :latitude
                    :longitude :number_of_reviews
                    :neighbourhood_group_cleansed :property_type)))

;; Now let's write another function to do a bit of summarization of the dataset.
;; We'll count the unique listing ids per host. This might show hosts that tends
;; that are treating their airbnb like a business... 
;;
(defn properties-per-host [df]
  (-> df
      (pt/melt {:id-vars :host_id :value-vars [:listing_id]})
      (pt/groupby :host_id)
      (pt/subset-cols :value)
      (pt/n-unique)
      (pt/data-frame)
      (py. sort_values :by :value :ascending false)
      (pt/rename {:columns {:value :num_unique_listings}})))

(-> listings-and-reviews
    properties-per-host)




