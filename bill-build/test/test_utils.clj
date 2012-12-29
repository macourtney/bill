(ns test-utils
  (:refer-clojure :exclude [clojure-version]))

(def bill-hash "e8f20161e67e2bf8d0c1c093a83dce07fd1e3c64")
(def bill-algorithm "SHA-1")
(def bill-version "0.0.1-SNAPSHOT")
(def bill-name 'org.bill/bill-build)

(def bill-jar-hash "5851c6476b61b07d7dd4792a14fc12089286ea75")

(def bill-dependency [bill-name bill-version bill-algorithm bill-hash])
(def bill-dependency-map { :group "org.bill" :artifact "bill-build" :version bill-version :algorithm bill-algorithm :hash bill-hash })

(def clojure-hash "c5aed5373965d1979dd249dd9f38d7bb5b2ee1c2")
(def clojure-algorithm "SHA-1")
(def clojure-version "1.4.0")
(def clojure-name 'org.clojure/clojure)
(def clojure-artifact "clojure")
(def clojure-group "org.clojure")

(def clojure-jar-hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9")

(def clojure-dependency [clojure-name clojure-version clojure-algorithm clojure-hash])
(def clojure-dependency-map { :group "org.clojure" :artifact "clojure" :version clojure-version :algorithm clojure-algorithm :hash clojure-hash })

(def fail-dependency-map { :artifact :fail :version :1.0.0 :algorithm "SHA-1" :hash :fail })

(def byte-char-set "UTF-8")
(def byte-array-class (Class/forName "[B"))