(ns bill.test.classpath
  (:refer-clojure :exclude [clojure-version])
  (:use clojure.test
        [bill.classpath :exclude [clojure-dependency]])
  (:require [bill.build :as build]
            [clojure.java.io :as java-io]
            [clojure.string :as string]))

(def bill-hash "ba0117d403574d3baa10a233624e9477845d4217")
(def bill-algorithm "SHA-1")
(def bill-version "0.0.1-SNAPSHOT")
(def bill-name 'org.bill/bill)

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

(deftest test-maven-file-name
  (is (= (maven-file-name { :artifact clojure-artifact :version clojure-version }) "clojure-1.4.0"))
  (is (= (maven-file-name { :artifact (keyword clojure-artifact) :version (keyword clojure-version) }) "clojure-1.4.0"))
  (is (nil? (maven-file-name { :artifact (keyword clojure-artifact) })))
  (is (nil? (maven-file-name { :version (keyword clojure-version) })))
  (is (nil? (maven-file-name {})))
  (is (nil? (maven-file-name nil))))
  
(defn assert-maven-jar [clojure-dependency-map]
  (let [clojure-jar (maven-jar clojure-dependency-map)]
    (is (= (str (maven-file-name clojure-dependency-map) ".jar") (.getName clojure-jar)))
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
    (is (= (str (maven-file-name clojure-dependency-map) ".pom") (.getName clojure-pom)))
    (is (= (maven-version-directory clojure-dependency-map) (.getParentFile clojure-pom)))))

(deftest test-maven-pom
  (assert-maven-pom { :group clojure-group :artifact clojure-artifact :version clojure-version })
  (assert-maven-pom { :group (keyword clojure-group) :artifact (keyword clojure-artifact) :version (keyword clojure-version) })
  (is (nil? (maven-pom nil))))

(deftest test-maven-pom?
  (is (maven-pom? { :group clojure-group :artifact clojure-artifact :version clojure-version }))
  (is (maven-pom? { :group (keyword clojure-group) :artifact (keyword clojure-artifact) :version (keyword clojure-version) }))
  (is (not (maven-pom? { :group :fail :artifact :fail :version :1.0.0 }))))
  
(defn assert-bill-algorithm-directory [clojure-dependency-map]
  (let [clojure-algorithm-directory (bill-algorithm-directory clojure-dependency-map)]
    (is (= (name (:algorithm clojure-dependency-map)) (.getName clojure-algorithm-directory)))
    (is (= bill-repository-directory (.getParentFile clojure-algorithm-directory)))))

(deftest test-bill-algorithm-directory
  (assert-bill-algorithm-directory { :algorithm clojure-algorithm })
  (assert-bill-algorithm-directory { :algorithm (keyword clojure-algorithm) })
  (is (nil? (bill-algorithm-directory nil))))
  
(defn assert-bill-hash-directory [clojure-dependency-map]
  (let [clojure-hash-directory (bill-hash-directory clojure-dependency-map)]
    (is (= (name (:hash clojure-dependency-map)) (.getName clojure-hash-directory)))
    (is (= (bill-algorithm-directory clojure-dependency-map) (.getParentFile clojure-hash-directory)))))

(deftest test-bill-hash-directory
  (assert-bill-hash-directory { :algorithm clojure-algorithm :hash clojure-hash })
  (assert-bill-hash-directory { :algorithm (keyword clojure-algorithm) :hash (keyword clojure-hash) })
  (is (nil? (bill-hash-directory nil))))
  
(defn assert-bill-jar [clojure-dependency-map]
  (let [clojure-jar (bill-jar clojure-dependency-map)]
    (is (= (str (maven-file-name clojure-dependency-map) ".jar") (.getName clojure-jar)))
    (is (= (bill-hash-directory clojure-dependency-map) (.getParentFile clojure-jar)))))

(deftest test-bill-jar
  (assert-bill-jar { :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash })
  (assert-bill-jar { :artifact (keyword clojure-artifact) :version (keyword clojure-version) :algorithm (keyword clojure-algorithm) :hash (keyword clojure-hash) })
  (is (nil? (bill-jar nil))))
  
(deftest test-bill-jar?
  (is (bill-jar? { :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash }))
  (is (bill-jar? { :artifact (keyword clojure-artifact) :version (keyword clojure-version) :algorithm (keyword clojure-algorithm) :hash (keyword clojure-hash) }))
  (is (not (bill-jar? { :artifact :fail :version :1.0.0 :algorithm (keyword clojure-algorithm) :hash :fail }))))

(defn assert-bill-clj [clojure-dependency-map]
  (let [clojure-clj (bill-clj clojure-dependency-map)]
    (is (= (str (maven-file-name clojure-dependency-map) ".clj") (.getName clojure-clj)))
    (is (= (bill-hash-directory clojure-dependency-map) (.getParentFile clojure-clj)))))
  
(deftest test-bill-clj
  (assert-bill-clj { :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash })
  (assert-bill-clj { :artifact (keyword clojure-artifact) :version (keyword clojure-version) :algorithm (keyword clojure-algorithm) :hash (keyword clojure-hash) })
  (is (nil? (bill-clj nil))))
  
(deftest test-bill-clj?
  (is (bill-clj? { :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash }))
  (is (bill-clj? { :artifact (keyword clojure-artifact) :version (keyword clojure-version) :algorithm (keyword clojure-algorithm) :hash (keyword clojure-hash) }))
  (is (not (bill-clj? fail-dependency-map))))

(deftest test-read-bill-clj
  (is (= (read-bill-clj clojure-dependency-map)
          { :group "org.clojure"
            :artifact "clojure"
            :version "1.4.0"
            :algorithm "SHA-1"
            :hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"
 
            :dependencies [] })))
  
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
  
(deftest test-read-byte-chunk
  (let [byte-chunk (read-byte-chunk (java-io/input-stream (.getBytes "blah" byte-char-set)) 4)]
    (is (= (count byte-chunk) 4))
    (is (instance? byte-array-class byte-chunk)))
  (let [byte-chunk (read-byte-chunk (java-io/input-stream (.getBytes "blahblah" byte-char-set)) 4)]
    (is (= (count byte-chunk) 4))
    (is (instance? byte-array-class byte-chunk)))
  (let [byte-chunk (read-byte-chunk (java-io/input-stream (.getBytes "blah" byte-char-set)) 8)]
    (is (= (count byte-chunk) 4))
    (is (instance? byte-array-class byte-chunk)))
  (let [byte-chunk (read-byte-chunk (java-io/input-stream (.getBytes "" byte-char-set)) 4)]
    (is (nil? byte-chunk))))

(deftest test-read-bytes
  (let [byte-seq (doall (read-bytes (java-io/input-stream (.getBytes "blahblah" byte-char-set)) 4))]
    (is byte-seq)
    (is (= (count byte-seq) 2))
    (doseq [byte-array byte-seq]
      (is (= (count byte-array) 4)))))

(deftest test-hash-code
  (let [clojure-jar (maven-jar (dependency-map [clojure-name clojure-version]))]
    (is clojure-jar)
    (is (.exists clojure-jar))
    (is (= (hash-code clojure-jar clojure-algorithm) clojure-hash)))
  (let [bill-jar (bill-jar bill-dependency-map)]
    (is bill-jar)
    (is (.exists bill-jar))
    (is (= (hash-code bill-jar bill-algorithm) bill-hash))))
    
(deftest test-validate-hash
  (is (validate-hash (maven-jar (dependency-map [clojure-name clojure-version])) clojure-algorithm clojure-hash))
  (is (validate-hash (bill-jar bill-dependency-map) bill-algorithm bill-hash))
  (is (not (validate-hash (maven-jar (dependency-map [clojure-name clojure-version])) clojure-algorithm "fail"))))
    
(deftest test-move-to-repository
  (let [clojure-jar (maven-jar clojure-dependency-map)
        bill-clojure-jar (bill-jar clojure-dependency-map)]
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
    (is (= (classpath) [(bill-jar clojure-dependency-map)]))
    (build/build!
      { :dependencies [bill-dependency] })
    (is (= (classpath) [(bill-jar clojure-dependency-map) (bill-jar bill-dependency-map)]))
    (build/build! old-build)))