(ns bill.test.core
  (:use clojure.test
        bill.core)
  (:require [bill.build :as build]))

(def project-name 'org.bill/bill)
(def project-version "0.0.1-SNAPSHOT")
(def description "A test build")
        
(deftest test-defbuild
  (let [old-build (build/build)]
    (defbuild
      { :project [org.bill/bill "0.0.1-SNAPSHOT"]
        :description "A test build" }
      
      (use 'clojure.test)
      (require ['bill.build :as 'build])
      
      (is (build/build)))
    (is (= { :project ['org.bill/bill project-version] :description description }
           (build/build)))
    (is (classloader))
    (classloader! nil)
    (build/build! old-build)))