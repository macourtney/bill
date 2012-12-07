(ns bill.test.classpath
  (:refer-clojure :exclude [clojure-version])
  (:use clojure.test
        bill.classpath)
  (:require [bill.build :as build]
            [bill.maven-repository :as maven-repository]
            [bill.repository :as repository]
            [clojure.java.io :as java-io]
            [clojure.string :as string]))

(def bill-hash "f05e9c95e2aac37f52c80d57c72890e54f361474")
(def bill-algorithm "SHA-1")
(def bill-version "0.0.1-SNAPSHOT")
(def bill-name 'org.bill/bill-build)

(def bill-dependency [bill-name bill-version bill-algorithm bill-hash])
(def bill-dependency-map (dependency-map bill-dependency))
            
(def clojure-hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9")
(def clojure-algorithm "SHA-1")
(def clojure-version "1.4.0")
(def clojure-name 'org.clojure/clojure)
(def clojure-artifact "clojure")
(def clojure-group "org.clojure")
(def byte-char-set "UTF-8")
(def byte-array-class (Class/forName "[B"))
            
(def clojure-dependency [clojure-name clojure-version clojure-algorithm clojure-hash])
            
(def clojure-dependency-map (dependency-map clojure-dependency))
(def fail-dependency-map { :artifact :fail :version :1.0.0 :algorithm "SHA-1" :hash :fail })

(deftest test-parse-hash-vector
  (is (= { :algorithm clojure-algorithm :hash clojure-hash }
         (parse-hash-vector [clojure-algorithm clojure-hash])))
  (is (= { :algorithm clojure-algorithm :hash clojure-hash }
         (parse-hash-vector [clojure-hash])))
  (is (nil? (parse-hash-vector [])))
  (is (nil? (parse-hash-vector nil))))

(deftest test-parse-dependency-symbol
  (is (= { :group clojure-group :artifact clojure-artifact }
         (parse-dependency-symbol clojure-name)))
  (is (= { :group "clojure" :artifact clojure-artifact }
         (parse-dependency-symbol 'clojure)))
  (is (nil? (parse-dependency-symbol nil))))
  
(deftest test-dependency-map
  (is (= { :group clojure-group :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash }
         (dependency-map [clojure-name clojure-version clojure-algorithm clojure-hash])))
  (is (= { :group "clojure" :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash }
         (dependency-map ['clojure clojure-version clojure-algorithm clojure-hash])))
  (is (= { :group "clojure" :artifact clojure-artifact :version clojure-version }
         (dependency-map ['clojure clojure-version])))
  (is (nil? (dependency-map nil))))

(deftest test-move-to-repository
  (let [clojure-jar (maven-repository/maven-jar clojure-dependency-map)
        bill-clojure-jar (repository/bill-jar clojure-dependency-map)]
    (when (.exists bill-clojure-jar)
      (.delete bill-clojure-jar))
    (move-to-repository clojure-jar clojure-algorithm)
    (is (.exists bill-clojure-jar))))

(deftest test-dependencies
  (is (= (dependencies clojure-dependency-map) []))
  (is (nil? (dependencies fail-dependency-map))))

(deftest test-group-artifact-str
  (is (= (group-artifact-str clojure-dependency-map) (str clojure-name)))
  (is (= (group-artifact-str { :artifact clojure-artifact }) (str clojure-artifact "/" clojure-artifact))))

(deftest test-create-classpath
  (is (= (create-classpath { :classpath {} 
                             :dependencies [clojure-dependency-map] })
         { (str clojure-name) clojure-dependency-map })))
  
(deftest test-classpath
  (let [old-build (build/build)]
    (build/build!
      { :dependencies [clojure-dependency] })
    (is (= (classpath [bill-dependency]) [(repository/bill-jar bill-dependency-map) (repository/bill-jar clojure-dependency-map)]))
    (build/build!
      { :dependencies [bill-dependency] })
    (is (= (classpath []) [(repository/bill-jar clojure-dependency-map) (repository/bill-jar bill-dependency-map)]))
    (build/build! old-build)))