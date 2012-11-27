(ns bill.test.classpath
  (:use clojure.test
        bill.classpath)
  (:require [bill.build :as build]
            [clojure.string :as string]))

(deftest test-user-directory
  (is user-directory)
  (is (.exists user-directory)))
  
(deftest test-maven-directory
  (is maven-directory)
  (is (.exists maven-directory)))

(deftest test-maven-repository-directory
  (is maven-repository-directory)
  (is (.exists maven-repository-directory)))
  
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
    
(defn assert-maven-group-directory [clojure-dependency-map]
  (let [clojure-group-directory (maven-group-directory clojure-dependency-map)
        group-directory-list (string/split (name (:group clojure-dependency-map)) #"\.")]
    (assert-directory-list group-directory-list clojure-group-directory)
    (is (= maven-repository-directory (parent-directory clojure-group-directory (count group-directory-list))))))
  
(deftest test-maven-group-directory
  (assert-maven-group-directory { :group "org.clojure" })
  (assert-maven-group-directory { :group :org.clojure }))

(defn assert-maven-artifact-directory [clojure-dependency-map]
  (let [clojure-artifact-directory (maven-artifact-directory clojure-dependency-map)]
    (is (= (name (:artifact clojure-dependency-map)) (.getName clojure-artifact-directory)))
    (is (= (maven-group-directory clojure-dependency-map) (.getParentFile clojure-artifact-directory)))))

(deftest test-maven-artifact-directory
  (assert-maven-artifact-directory { :group "org.clojure" :artifact "clojure" })
  (assert-maven-artifact-directory { :group :org.clojure :artifact :clojure }))
  
(defn assert-maven-version-directory [clojure-dependency-map]
  (let [clojure-version-directory (maven-version-directory clojure-dependency-map)]
    (is (= (name (:version clojure-dependency-map)) (.getName clojure-version-directory)))
    (is (= (maven-artifact-directory clojure-dependency-map) (.getParentFile clojure-version-directory)))))

(deftest test-maven-version-directory
  (assert-maven-version-directory { :group "org.clojure" :artifact "clojure" :version "1.4.0" })
  (assert-maven-version-directory { :group :org.clojure :artifact :clojure :version :1.4.0 }))

(deftest test-maven-file-name
  (is (= (maven-file-name { :artifact "clojure" :version "1.4.0" }) "clojure-1.4.0"))
  (is (= (maven-file-name { :artifact :clojure :version :1.4.0 }) "clojure-1.4.0"))
  (is (nil? (maven-file-name { :artifact :clojure })))
  (is (nil? (maven-file-name { :version :1.4.0 })))
  (is (nil? (maven-file-name {})))
  (is (nil? (maven-file-name nil))))
  
(defn assert-maven-jar [clojure-dependency-map]
  (let [clojure-jar (maven-jar clojure-dependency-map)]
    (is (= (str (maven-file-name clojure-dependency-map) ".jar") (.getName clojure-jar)))
    (is (= (maven-version-directory clojure-dependency-map) (.getParentFile clojure-jar)))))

(deftest test-maven-jar
  (assert-maven-jar { :group "org.clojure" :artifact "clojure" :version "1.4.0" })
  (assert-maven-jar { :group :org.clojure :artifact :clojure :version :1.4.0 })
  (is (nil? (maven-jar nil))))
  
(defn assert-maven-pom [clojure-dependency-map]
  (let [clojure-pom (maven-pom clojure-dependency-map)]
    (is (= (str (maven-file-name clojure-dependency-map) ".pom") (.getName clojure-pom)))
    (is (= (maven-version-directory clojure-dependency-map) (.getParentFile clojure-pom)))))

(deftest test-maven-pom
  (assert-maven-pom { :group "org.clojure" :artifact "clojure" :version "1.4.0" })
  (assert-maven-pom { :group :org.clojure :artifact :clojure :version :1.4.0 })
  (is (nil? (maven-pom nil))))
  
(deftest test-classpath
  (let [old-build (build/build)]
    (build/build!
      { :dependencies ['org.clojure/clojure "1.4.0"] })
    (is (= [] (classpath)))
    (build/build! old-build)))