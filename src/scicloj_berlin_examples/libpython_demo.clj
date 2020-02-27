(ns libpython-demo
  (:import java.lang.Iterable)
  (:require [libpython-clj.python :as py :refer [py. py.- py..]]
            [libpython-clj.require :refer [require-python]]
            [clojure.pprint :refer [pprint]]))

(py/initialize!)

;; Okay so what can we do with libpython-clj?

;; We can start with a Clojure/Java thing, say a number
;; and send it to the Python process. I.e. "copy"
(py/->python 5334)

;; This copying is limited, though. We can't do much.
;; It's a pointer after all.
(->> 5334 py/->python (+ 1))

;; Luckily, libpython-clj doesn't leave it here. It does
;; something called "bridging". With bridging, we get the
;; item in a form that allows us just to use it! 
(->> 4334
     py/->python
     py/as-jvm
     (* 2))

;; And this also works with clj data structures
(->> [1 -3 4]
     py/->python
     py/as-jvm
     (map inc))

;; Let's look at what these things are.

;; This is now a linked type -- not sure how this works
(->> 4334
     py/->python
     py/as-jvm
     type)

;; This is now a "wrapped" object, a :pyobject
(->> [1 -3 4]
     py/->python
     py/as-jvm
     type)

;; The wrapped or bridged python objects have some associated helper methods
;; that make it possible to work with these python objects.

;; Note: we don't have to do all the steps I've been showing
;; to ge the bridged item.
;; libpython-clj tends to default to "bridged mode". I.e.
;; we have helpers to create specific types that just give you
;; the bridged to that type.
(def bridged-python-thing
  (->> [1 -3 4] py/->py-list))

;; So let's see what we can do with this bridged :pyobject wrapper thing

;; We can look at the attributes of the underlying python objects.
(->> bridged-python-thing
     py/att-type-map)

;; We can call python functions on the bridged list
;; python equivalent: [1 -3 4].insert(0, 100)
(-> bridged-python-thing
    (py/call-attr "insert" 0 100))

;; There is also some syntactic sugar to do this more
;; seamlessly.
(py. bridged-python-thing "insert" 0 2000)
(py.- bridged-python-thing "__doc__")


;; To finish this section off, here's one more demo to illustrate
;; the fluidity of the interop that's this library achieves.

;; We can copy things to python, but we can also
;; go the other way. Everything in python is just
;; a map, even its libraries. B/c they are just
;; data structures we can connect to them as well.
(def py-builtins (py/import-module "builtins"))
(py/att-type-map py-builtins)

;; We can atually do this more easily using a familiar
;; form, require:
(require-python '[builtins :as py-builtins])
(py/att-type-map py-builtins)

;; And now we can do this:
(->> [1 -3 4]
     py/->py-list
     (py-builtins/map inc)
     (filter pos?))



