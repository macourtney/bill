(ns bill.test.repository
  (:refer-clojure :exclude [clojure-version])
  (:use clojure.test
        bill.repository)
  (:require [bill.build :as build]
            [bill.classpath :as classpath]
            [bill.util :as util]
            [clojure.java.io :as java-io]
            [clojure.string :as string]
            test-utils))

(def fail-dependency-map { :artifact :fail :version :1.0.0 :algorithm "SHA-1" :hash :fail })

(def clojure-clj-map { :group test-utils/clojure-group
                       :artifact test-utils/clojure-artifact
                       :version test-utils/clojure-version

                       :file { :name "clojure-1.4.0.jar"
                               :algorithm "SHA-1"
                               :hash test-utils/clojure-jar-hash }

                       :dependencies [] })

(def test-clj (java-io/file "test/clojure-1.4.0.clj"))

(deftest test-bill-directory
  (is bill-directory)
  (is (.exists bill-directory)))
  
(deftest test-bill-repository-directory
  (is bill-repository-directory)
  (is (.exists bill-repository-directory)))

(defn assert-directory-list [directory-list directory]
  (when (not-empty directory-list)
    (is (= (last directory-list) (.getName directory)))
    (assert-directory-list (butlast directory-list) (.getParentFile directory))))

(defn parent-directory [directory parent-count]
  (if (not (pos? parent-count))
    directory
    (recur (.getParentFile directory) (dec parent-count))))

(defn assert-bill-algorithm-directory [clojure-dependency-map]
  (let [clojure-algorithm-directory (bill-algorithm-directory clojure-dependency-map)]
    (is (= (name (:algorithm clojure-dependency-map)) (.getName clojure-algorithm-directory)))
    (is (= bill-repository-directory (.getParentFile clojure-algorithm-directory)))))

(deftest test-bill-algorithm-directory
  (assert-bill-algorithm-directory { :algorithm test-utils/clojure-algorithm })
  (assert-bill-algorithm-directory { :algorithm (keyword test-utils/clojure-algorithm) })
  (is (nil? (bill-algorithm-directory nil))))
  
(defn assert-bill-hash-directory [clojure-dependency-map]
  (let [clojure-hash-directory (bill-hash-directory clojure-dependency-map)]
    (is (= (name (:hash clojure-dependency-map)) (.getName clojure-hash-directory)))
    (is (= (bill-algorithm-directory clojure-dependency-map) (.getParentFile clojure-hash-directory)))))

(deftest test-bill-hash-directory
  (assert-bill-hash-directory { :algorithm test-utils/clojure-algorithm :hash test-utils/clojure-hash })
  (assert-bill-hash-directory { :algorithm (keyword test-utils/clojure-algorithm) :hash (keyword test-utils/clojure-hash) })
  (is (nil? (bill-hash-directory nil))))
  
(defn assert-bill-jar [clojure-dependency-map]
  (let [clojure-jar (bill-jar clojure-dependency-map)]
    (is (= (str (util/file-name clojure-dependency-map) ".jar") (.getName clojure-jar)))
    (is (= (bill-hash-directory clojure-dependency-map) (.getParentFile clojure-jar)))))

(deftest test-bill-jar
  (assert-bill-jar { :artifact test-utils/clojure-artifact :version test-utils/clojure-version :algorithm test-utils/clojure-algorithm :hash test-utils/clojure-hash })
  (assert-bill-jar { :artifact (keyword test-utils/clojure-artifact) :version (keyword test-utils/clojure-version) :algorithm (keyword test-utils/clojure-algorithm) :hash (keyword test-utils/clojure-hash) })
  (is (nil? (bill-jar nil))))
  
(deftest test-bill-jar?
  (is (bill-jar? { :artifact test-utils/clojure-artifact :version test-utils/clojure-version :algorithm test-utils/clojure-algorithm :hash test-utils/clojure-hash }))
  (is (bill-jar? { :artifact (keyword test-utils/clojure-artifact) :version (keyword test-utils/clojure-version) :algorithm (keyword test-utils/clojure-algorithm) :hash (keyword test-utils/clojure-hash) }))
  (is (not (bill-jar? { :artifact :fail :version :1.0.0 :algorithm (keyword test-utils/clojure-algorithm) :hash :fail }))))

(defn assert-bill-clj [clojure-dependency-map]
  (let [clojure-clj (bill-clj clojure-dependency-map)]
    (is (= (str (util/file-name clojure-dependency-map) ".clj") (.getName clojure-clj)))
    (is (= (bill-hash-directory clojure-dependency-map) (.getParentFile clojure-clj)))))
  
(deftest test-bill-clj
  (assert-bill-clj { :artifact test-utils/clojure-artifact :version test-utils/clojure-version :algorithm test-utils/clojure-algorithm :hash test-utils/clojure-hash })
  (assert-bill-clj { :artifact (keyword test-utils/clojure-artifact) :version (keyword test-utils/clojure-version) :algorithm (keyword test-utils/clojure-algorithm) :hash (keyword test-utils/clojure-hash) })
  (is (nil? (bill-clj nil))))
  
(deftest test-bill-clj?
  (is (bill-clj? { :artifact test-utils/clojure-artifact :version test-utils/clojure-version :algorithm test-utils/clojure-algorithm :hash test-utils/clojure-hash }))
  (is (bill-clj? { :artifact (keyword test-utils/clojure-artifact) :version (keyword test-utils/clojure-version) :algorithm (keyword test-utils/clojure-algorithm) :hash (keyword test-utils/clojure-hash) }))
  (is (not (bill-clj? fail-dependency-map))))

(deftest test-read-bill-clj-file
  (is (= (read-bill-clj-file (bill-clj test-utils/clojure-dependency-map))
          clojure-clj-map)))
  
(deftest test-read-bill-clj
  (is (= (read-bill-clj test-utils/clojure-dependency-map)
          clojure-clj-map)))

(deftest test-write-bill-clj
  (try
    (is (not (.exists test-clj)))
    (write-bill-clj test-clj clojure-clj-map)
    (is (.exists test-clj))
    (finally
      (.delete test-clj))))