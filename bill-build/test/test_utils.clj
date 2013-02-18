(ns test-utils
  (:refer-clojure :exclude [clojure-version])
  (:require [bill.build :as build]
            [bill.core :as bill]
            [bill.repository :as repository]
            [bill.util :as util]))

(defn dependency? [dependency-name-symbol dependency-vector]
  (when (= (first dependency-vector) dependency-name-symbol)
    dependency-vector))

(defn find-dependency [dependency-name-symbol]
  (some #(dependency? dependency-name-symbol %1) (build/dependencies)))

(defn find-jar-hash [dependency-map]
  (:hash (:file (repository/read-bill-clj dependency-map))))
  
(def bill-dependency bill/bill-dependency)
(def bill-dependency-map (util/dependency-map bill-dependency))

(def bill-name (first bill-dependency))
(def bill-version (:version bill-dependency-map))
(def bill-algorithm (:algorithm bill-dependency-map))
(def bill-hash (:hash bill-dependency-map))

(def bill-jar-hash (find-jar-hash bill-dependency-map))

(def clojure-dependency (find-dependency 'org.clojure/clojure))
(def clojure-dependency-map (util/dependency-map clojure-dependency))

(def clojure-group (:group clojure-dependency-map))
(def clojure-artifact (:artifact clojure-dependency-map))
(def clojure-name (first clojure-dependency))
(def clojure-version (:version clojure-dependency-map))
(def clojure-algorithm (:algorithm clojure-dependency-map))
(def clojure-hash (:hash clojure-dependency-map))

(def clojure-jar-hash (find-jar-hash clojure-dependency-map))

(def tools-namespace-dependency (find-dependency 'org.clojure/tools.namespace))
(def tools-namespace-dependency-map (util/dependency-map tools-namespace-dependency))

(def tools-namespace-group (:group tools-namespace-dependency-map))
(def tools-namespace-artifact (:artifact tools-namespace-dependency-map))
(def tools-namespace-name (first tools-namespace-dependency))
(def tools-namespace-version (:version tools-namespace-dependency-map))
(def tools-namespace-algorithm (:algorithm tools-namespace-dependency-map))
(def tools-namespace-hash (:hash tools-namespace-dependency-map))

(def fail-dependency-map { :artifact :fail :version :1.0.0 :algorithm "SHA-1" :hash :fail })

(def byte-char-set "UTF-8")
(def byte-array-class (Class/forName "[B"))