(ns test-utils
  (:refer-clojure :exclude [clojure-version]))

(def bill-hash "13404996ae75e735e5b14a4af6e674f1d212a744")
(def bill-algorithm "SHA-1")
(def bill-version "0.0.1-SNAPSHOT")
(def bill-name 'org.bill/bill-build)

(def bill-jar-hash "fe1d2a4fbfb1b6caa5dee8bac30ba62ac7ca44fc")

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
(def clojure-dependency-map { :group clojure-group :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash })

(def tools-namespace-hash "4feabeada43d50bf6e90acd3a7fc7148e288dbc1")
(def tools-namespace-algorithm "SHA-1")
(def tools-namespace-version "0.2.2")
(def tools-namespace-name 'org.clojure/tools.namespace)
(def tools-namespace-artifact "tools.namespace")
(def tools-namespace-group "org.clojure")

(def tools-namespace-dependency [tools-namespace-name tools-namespace-version tools-namespace-algorithm tools-namespace-hash])
(def tools-namespace-dependency-map { :group tools-namespace-group :artifact tools-namespace-artifact :version tools-namespace-version :algorithm tools-namespace-algorithm :hash tools-namespace-hash })

(def fail-dependency-map { :artifact :fail :version :1.0.0 :algorithm "SHA-1" :hash :fail })

(def byte-char-set "UTF-8")
(def byte-array-class (Class/forName "[B"))