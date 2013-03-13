(ns bill.tasks.test.uberjar
  (:use clojure.test
        bill.tasks.uberjar)
  (:require [bill.build :as build]
            [bill.repository :as repository]
            [bill.task :as task]
            test-utils)
  (:import [java.util.zip ZipFile]))

(def dependencies-vector [test-utils/clojure-dependency])
(def test-uberjar-name "test_uberjar.jar")
(def test-build-map (merge build/build-defaults
                      { :project ['org.bill/bill "0.0.1-SNAPSHOT"]
                        :dependencies dependencies-vector
                        :uberjar-name test-uberjar-name }))

(defn build-fixture [test]
  (build/build! test-build-map)
  (test)
  (build/build! build/build-defaults))

(use-fixtures :once build-fixture)

(deftest test-init
  (require 'bill.tasks.uberjar :reload)
  (is (task/find-task :uberjar)))
  
(deftest test-jar-dependencies
  (is (= (jar-dependencies) [test-utils/clojure-jar])))

(deftest test-jars
  (is (= (jars) [(build/target-jar-file) test-utils/clojure-jar])))
  
(deftest test-uberjar-stream
  (let [uberjar-file (build/target-uberjar-file)]
    (is (not (.exists uberjar-file)))
    (with-open [uberjar-stream (uberjar-stream)]
      (is uberjar-stream))
    (is (.exists uberjar-file))
    (.delete uberjar-file)
    (is (not (.exists uberjar-file)))))
  
(deftest test-jar-entries
  (let [clojure-entries (jar-entries (ZipFile. test-utils/clojure-jar))]
    (is clojure-entries)
    (is (= (count clojure-entries) 3029)))
  (is (nil? (jar-entries nil))))
  
(deftest test-include-entry?
  (let [clojure-entries (jar-entries (ZipFile. test-utils/clojure-jar))
        test-entry (first clojure-entries)
        already-included (set (map #(.getName %) (take 10 (rest clojure-entries))))]
    (is (include-entry? test-entry already-included))
    (is (include-entry? test-entry nil))
    (is (not (include-entry? (second clojure-entries) already-included)))
    (is (not (include-entry? nil already-included)))
    (build/update-build! { :uberjar-exclusions [#"META-INF/"] })
    (is (not (include-entry? test-entry already-included)))
    (build/build! test-build-map)))

(deftest test-write-entry
  (let [clojure-zip-file (ZipFile. test-utils/clojure-jar)
        clojure-entries (jar-entries clojure-zip-file)
        test-entry (second clojure-entries)
        uberjar-file (build/target-uberjar-file)]
    (is (not (.exists uberjar-file)))
    (is (= (.getName uberjar-file) test-uberjar-name))
    (with-open [uberjar-stream (uberjar-stream)]
      (write-entry uberjar-stream clojure-zip-file test-entry))
    (is (.exists uberjar-file))
    (is (.delete uberjar-file))))

(deftest test-write-jar
  (let [uberjar-file (build/target-uberjar-file)]
    (is (not (.exists uberjar-file)))
    (with-open [uberjar-stream (uberjar-stream)]
      (write-jar uberjar-stream test-utils/clojure-jar #{}))
    (is (.exists uberjar-file))
    (with-open [clojure-jar-zip-file (ZipFile. test-utils/clojure-jar)
                uberjar-zip-file (ZipFile. uberjar-file)]
      (is (= (map #(.getName %) (jar-entries clojure-jar-zip-file))
             (map #(.getName %) (jar-entries uberjar-zip-file)))))
    (is (.delete uberjar-file) (str "Could not delete " (.getAbsolutePath uberjar-file)))))

(deftest test-write-jars
  (let [uberjar-file (build/target-uberjar-file)]
    (is (not (.exists uberjar-file)))
    (with-open [uberjar-stream (uberjar-stream)]
      (write-jars uberjar-stream))
    (is (.exists uberjar-file))
    (is (.delete uberjar-file))))

(deftest test-write-uberjar
  (let [uberjar-file (build/target-uberjar-file)]
    (is (not (.exists uberjar-file)))
    (write-uberjar)
    (is (.exists uberjar-file))
    (is (.delete uberjar-file))))