(ns libpython-numpy-demo
  (:import java.lang.Iterable)
  (:require [libpython-clj.python :as py :refer [py. py.- py..]]
            [libpython-clj.require :refer [require-python]]
            [tech.v2.datatype :as dtype]
            [tech.v2.datatype.functional :as dfn]
            [clojure.pprint :refer [pprint]]))

(py/initialize!)

;; Previously, we toured libpython-clj's facilities for python interop.
;; We looked at these particulary with primitives and basic data structures
;; like lists.
;;
;; Importantly, however, for our story, libpython-clj also supports n-dimensional
;; data of any type. This means that we can work fluidly with core data science
;; libraries in the Python world such as numpy and pandas.

;; Let's explore what this support means...

(require-python '[numpy :as np])

;; We can create a multi-dimensional array using numpy
(np/ones [2 3])


;; We can work on this thing with some basic clojure functions
(-> (np/ones [2 3])
    first)

(->> (np/ones [2 3])
     (map (fn [x]
            (map inc x))))

;; And of course we can use numpy functions
(->> (np/ones [2 3])
     (np/dot 3))

;; So why does this work? The reason is because of
;; the magic unicorn 🦄 library call tech.datatype
;;
;; Datatype provides some abstractions that allow us to
;; work with collections of data in a very general way.
;;

;; Containers - collections that can be of many type, backed by different storage
;;              mechanisms. 
(dtype/make-container :typed-buffer :uint8 (range 5))    ;; tech.datatype buffer.
(dtype/make-container :native-buffer :float32 (range 5)) ;; nio buffer

;; Readers - The magic part. Works on persistent vectors, java arrays, nio buffers, etc.
;;           Anything that derives from java.util.RandomAccess (which implies a random access list).
;;
;; These are the exchange mechanism that make these containers accessible to 
(->> (dtype/make-container :typed-buffer :float32 (range 5))
     dtype/->reader
     (map inc))

;; And now also we have a functions to do some common operations
(->> (dtype/make-container :typed-buffer :float32 (range 5))
     dtype/->reader
     (dfn/+ 1)
     (dfn/mean))

;; And we can use Numpy functions!
(->> (dtype/make-container :typed-buffer :float32 (range 5))
     dtype/->reader
     (np/mean))




