(ns panthera-demo
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [panthera.panthera :as pt]
            [panthera.pandas.utils :as pdutils]
            [clojure.pprint :refer [pprint print-table]]))

(py/initialize!)
(require-python '[pandas :as pd])
(require-python '[numpy :as np])

(defn data-source [filename]
  (apply str "data/berlin-airbnb-data/" filename))

(def reviews-summary
  (pd/read_csv (data-source "reviews_summary.csv")))
(def listings-summary
  (pd/read_csv (data-source "listings_summary.csv")))

(defn merge-listings-and-reviews [ds1 ds2]
  (-> (pt/merge-ordered ds1 ds2 {:left-on :listing_id :right-on :id :how :left})
      (pt/rename {:columns {:id_x :id :neighbourhood_group_cleansed :neighbourhood_group}})
      (pt/drop-cols :id_y)))

(def listings-and-reviews
  (merge-listings-and-reviews
    reviews-summary
    (pt/subset-cols listings-summary
                    :id :host_id :latitude
                    :longitude :number_of_reviews
                    :neighbourhood_group_cleansed :property_type)))

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




