(ns bill.test.maven-repository
  (:refer-clojure :exclude [clojure-version])
  (:use clojure.test
        bill.maven-repository)
  (:require [bill.build :as build]
            [bill.classpath :as classpath]
            [bill.util :as util]
            [clojure.java.io :as java-io]
            [clojure.string :as string]))

(def clojure-version "1.4.0")
(def clojure-artifact "clojure")
(def clojure-group "org.clojure")

(deftest test-maven-directory
  (is maven-directory)
  (is (.exists maven-directory)))

(deftest test-maven-repository-directory
  (is maven-repository-directory)
  (is (.exists maven-repository-directory)))

(defn assert-directory-list [directory-list directory]
  (when (not-empty directory-list)
    (is (= (last directory-list) (.getName directory)))
    (assert-directory-list (butlast directory-list) (.getParentFile directory))))

(defn parent-directory [directory parent-count]
  (if (not (pos? parent-count))
    directory
    (recur (.getParentFile directory) (dec parent-count))))
    
(defn assert-maven-group-directory [clojure-dependency-map]
  (let [clojure-group-directory (maven-group-directory clojure-dependency-map)
        group-directory-list (string/split (name (:group clojure-dependency-map)) #"\.")]
    (assert-directory-list group-directory-list clojure-group-directory)
    (is (= maven-repository-directory (parent-directory clojure-group-directory (count group-directory-list))))))
  
(deftest test-maven-group-directory
  (assert-maven-group-directory { :group clojure-group })
  (assert-maven-group-directory { :group (keyword clojure-group) }))

(defn assert-maven-artifact-directory [clojure-dependency-map]
  (let [clojure-artifact-directory (maven-artifact-directory clojure-dependency-map)]
    (is (= (name (:artifact clojure-dependency-map)) (.getName clojure-artifact-directory)))
    (is (= (maven-group-directory clojure-dependency-map) (.getParentFile clojure-artifact-directory)))))

(deftest test-maven-artifact-directory
  (assert-maven-artifact-directory { :group clojure-group :artifact clojure-artifact })
  (assert-maven-artifact-directory { :group (keyword clojure-group) :artifact (keyword clojure-artifact) }))
  
(defn assert-maven-version-directory [clojure-dependency-map]
  (let [clojure-version-directory (maven-version-directory clojure-dependency-map)]
    (is (= (name (:version clojure-dependency-map)) (.getName clojure-version-directory)))
    (is (= (maven-artifact-directory clojure-dependency-map) (.getParentFile clojure-version-directory)))))

(deftest test-maven-version-directory
  (assert-maven-version-directory { :group clojure-group :artifact clojure-artifact :version clojure-version })
  (assert-maven-version-directory { :group (keyword clojure-group) :artifact (keyword clojure-artifact) :version (keyword clojure-version) }))
  
(defn assert-maven-jar [clojure-dependency-map]
  (let [clojure-jar (maven-jar clojure-dependency-map)]
    (is (= (str (util/file-name clojure-dependency-map) ".jar") (.getName clojure-jar)))
    (is (= (maven-version-directory clojure-dependency-map) (.getParentFile clojure-jar)))))

(deftest test-maven-jar
  (assert-maven-jar { :group clojure-group :artifact clojure-artifact :version clojure-version })
  (assert-maven-jar { :group (keyword clojure-group) :artifact (keyword clojure-artifact) :version (keyword clojure-version) })
  (is (nil? (maven-jar nil))))
  
(deftest test-maven-jar?
  (is (maven-jar? { :group clojure-group :artifact clojure-artifact :version clojure-version }))
  (is (maven-jar? { :group (keyword clojure-group) :artifact (keyword clojure-artifact) :version (keyword clojure-version) }))
  (is (not (maven-jar? { :group :fail :artifact :fail :version :1.0.0 }))))
  
(defn assert-maven-pom [clojure-dependency-map]
  (let [clojure-pom (maven-pom clojure-dependency-map)]
    (is (= (str (util/file-name clojure-dependency-map) ".pom") (.getName clojure-pom)))
    (is (= (maven-version-directory clojure-dependency-map) (.getParentFile clojure-pom)))))

(deftest test-maven-pom
  (assert-maven-pom { :group clojure-group :artifact clojure-artifact :version clojure-version })
  (assert-maven-pom { :group (keyword clojure-group) :artifact (keyword clojure-artifact) :version (keyword clojure-version) })
  (is (nil? (maven-pom nil))))

(deftest test-maven-pom?
  (is (maven-pom? { :group clojure-group :artifact clojure-artifact :version clojure-version }))
  (is (maven-pom? { :group (keyword clojure-group) :artifact (keyword clojure-artifact) :version (keyword clojure-version) }))
  (is (not (maven-pom? { :group :fail :artifact :fail :version :1.0.0 }))))

(deftest test-convert-to-bill-clj
  (let [dependency-map { :group clojure-group :artifact clojure-artifact :version clojure-version }]
    (is (= (convert-to-bill-clj dependency-map)
         { :group clojure-group
           :artifact clojure-artifact
           :version clojure-version
           :file { :name (str clojure-artifact "-" clojure-version ".jar")
                   :algorithm util/default-algorithm
                   :hash (util/hash-code (maven-jar dependency-map) util/default-algorithm) }

           :dependencies []}))))