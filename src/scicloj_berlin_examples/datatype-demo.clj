(ns datatype-demo
  (:import java.lang.Iterable)
  (:require [tech.v2.datatype :as dtype]))

(def test-container (dtype/make-container :typed-buffer :uint8 (range 5)))

(->> test-container
     dtype/->reader
     (instance? Iterable)
     )
