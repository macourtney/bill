(ns bill.test.classpath
  (:refer-clojure :exclude [test-utils/clojure-version])
  (:use clojure.test
        bill.classpath)
  (:require [bill.build :as build]
            [bill.maven-repository :as maven-repository]
            [bill.repository :as repository]
            [clojure.java.io :as java-io]
            [clojure.string :as string]
            test-utils))

(deftest test-dependencies
  (is (= (dependencies test-utils/clojure-dependency-map) nil))
  (is (nil? (dependencies test-utils/fail-dependency-map))))

(deftest test-group-artifact-str
  (is (= (group-artifact-str test-utils/clojure-dependency-map) (str test-utils/clojure-name)))
  (is (= (group-artifact-str { :artifact test-utils/clojure-artifact }) (str test-utils/clojure-artifact "/" test-utils/clojure-artifact))))

(deftest test-create-classpath
  (is (= (create-classpath { :classpath {} 
                             :dependencies [test-utils/clojure-dependency-map] })
         { (str test-utils/clojure-name) test-utils/clojure-dependency-map })))

(deftest test-dependencies
  (is (= (dependencies test-utils/bill-dependency-map) [test-utils/clojure-dependency])))

(deftest test-child-dependency-maps
  (is (= (child-dependency-maps test-utils/bill-dependency-map) [test-utils/clojure-dependency-map])))

(deftest test-classpath
  (let [old-build (build/build)]
    (build/build!
      { :dependencies [test-utils/clojure-dependency] })
    (is (= (classpath [test-utils/bill-dependency]) 
           [(repository/bill-jar test-utils/bill-dependency-map) (repository/bill-jar test-utils/clojure-dependency-map)]))
    (build/build!
      { :dependencies [test-utils/bill-dependency] })
    (is (= (classpath []) [(repository/bill-jar test-utils/clojure-dependency-map) (repository/bill-jar test-utils/bill-dependency-map)]))
    (build/build! old-build)))