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
  
(deftest test-maven-jar?
  (is (maven-jar? { :group "org.clojure" :artifact "clojure" :version "1.4.0" }))
  (is (maven-jar? { :group :org.clojure :artifact :clojure :version :1.4.0 }))
  (is (not (maven-jar? { :group :fail :artifact :fail :version :1.0.0 }))))
  
(defn assert-maven-pom [clojure-dependency-map]
  (let [clojure-pom (maven-pom clojure-dependency-map)]
    (is (= (str (maven-file-name clojure-dependency-map) ".pom") (.getName clojure-pom)))
    (is (= (maven-version-directory clojure-dependency-map) (.getParentFile clojure-pom)))))

(deftest test-maven-pom
  (assert-maven-pom { :group "org.clojure" :artifact "clojure" :version "1.4.0" })
  (assert-maven-pom { :group :org.clojure :artifact :clojure :version :1.4.0 })
  (is (nil? (maven-pom nil))))

(deftest test-maven-pom?
  (is (maven-pom? { :group "org.clojure" :artifact "clojure" :version "1.4.0" }))
  (is (maven-pom? { :group :org.clojure :artifact :clojure :version :1.4.0 }))
  (is (not (maven-pom? { :group :fail :artifact :fail :version :1.0.0 }))))
  
(defn assert-bill-algorithm-directory [clojure-dependency-map]
  (let [clojure-algorithm-directory (bill-algorithm-directory clojure-dependency-map)]
    (is (= (name (:algorithm clojure-dependency-map)) (.getName clojure-algorithm-directory)))
    (is (= bill-repository-directory (.getParentFile clojure-algorithm-directory)))))

(deftest test-bill-algorithm-directory
  (assert-bill-algorithm-directory { :algorithm "SHA-1" })
  (assert-bill-algorithm-directory { :algorithm :SHA-1 })
  (is (nil? (bill-algorithm-directory nil))))
  
(defn assert-bill-hash-directory [clojure-dependency-map]
  (let [clojure-hash-directory (bill-hash-directory clojure-dependency-map)]
    (is (= (name (:hash clojure-dependency-map)) (.getName clojure-hash-directory)))
    (is (= (bill-algorithm-directory clojure-dependency-map) (.getParentFile clojure-hash-directory)))))

(deftest test-bill-hash-directory
  (assert-bill-hash-directory { :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" })
  (assert-bill-hash-directory { :algorithm :SHA-1 :hash :867288bc07a6514e2e0b471c5be0bccd6c3a51f9 })
  (is (nil? (bill-hash-directory nil))))
  
(defn assert-bill-jar [clojure-dependency-map]
  (let [clojure-jar (bill-jar clojure-dependency-map)]
    (is (= (str (maven-file-name clojure-dependency-map) ".jar") (.getName clojure-jar)))
    (is (= (bill-hash-directory clojure-dependency-map) (.getParentFile clojure-jar)))))

(deftest test-bill-jar
  (assert-bill-jar { :artifact "clojure" :version "1.4.0" :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" })
  (assert-bill-jar { :artifact :clojure :version :1.4.0 :algorithm :SHA-1 :hash :867288bc07a6514e2e0b471c5be0bccd6c3a51f9 })
  (is (nil? (bill-jar nil))))
  
(deftest test-bill-jar?
  (is (bill-jar? { :artifact "clojure" :version "1.4.0" :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" }))
  (is (bill-jar? { :artifact :clojure :version :1.4.0 :algorithm :SHA-1 :hash :867288bc07a6514e2e0b471c5be0bccd6c3a51f9 }))
  (is (not (bill-jar? { :artifact :fail :version :1.0.0 :algorithm :SHA-1 :hash :fail }))))

(defn assert-bill-clj [clojure-dependency-map]
  (let [clojure-clj (bill-clj clojure-dependency-map)]
    (is (= (str (maven-file-name clojure-dependency-map) ".clj") (.getName clojure-clj)))
    (is (= (bill-hash-directory clojure-dependency-map) (.getParentFile clojure-clj)))))
  
(deftest test-bill-clj
  (assert-bill-clj { :artifact "clojure" :version "1.4.0" :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" })
  (assert-bill-clj { :artifact :clojure :version :1.4.0 :algorithm :SHA-1 :hash :867288bc07a6514e2e0b471c5be0bccd6c3a51f9 })
  (is (nil? (bill-clj nil))))
  
(deftest test-bill-clj?
  (is (bill-clj? { :artifact "clojure" :version "1.4.0" :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" }))
  (is (bill-clj? { :artifact :clojure :version :1.4.0 :algorithm :SHA-1 :hash :867288bc07a6514e2e0b471c5be0bccd6c3a51f9 }))
  (is (not (bill-clj? { :artifact :fail :version :1.0.0 :algorithm :SHA-1 :hash :fail }))))

(deftest test-parse-hash-vector
  (is (= { :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" }
         (parse-hash-vector ["SHA-1" "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"])))
  (is (= { :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" }
         (parse-hash-vector ["867288bc07a6514e2e0b471c5be0bccd6c3a51f9"])))
  (is (nil? (parse-hash-vector [])))
  (is (nil? (parse-hash-vector nil))))

(deftest test-parse-dependency-symbol
  (is (= { :group "org.clojure" :artifact "clojure" }
         (parse-dependency-symbol 'org.clojure/clojure)))
  (is (= { :group "clojure" :artifact "clojure" }
         (parse-dependency-symbol 'clojure)))
  (is (nil? (parse-dependency-symbol nil))))
  
(deftest test-dependency-map
  (is (= { :group "org.clojure" :artifact "clojure" :version "1.4.0" :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" }
         (dependency-map ['org.clojure/clojure "1.4.0" "SHA-1" "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"])))
  (is (= { :group "clojure" :artifact "clojure" :version "1.4.0" :algorithm "SHA-1" :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9" }
         (dependency-map ['clojure "1.4.0" "SHA-1" "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"])))
  (is (= { :group "clojure" :artifact "clojure" :version "1.4.0" }
         (dependency-map ['clojure "1.4.0"])))
  (is (nil? (dependency-map nil))))
  
(deftest test-classpath
  (let [old-build (build/build)]
    (build/build!
      { :dependencies ['org.clojure/clojure "1.4.0"] })
    (is (= [] (classpath)))
    (build/build! old-build)))