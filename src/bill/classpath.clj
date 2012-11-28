(ns bill.classpath
  (:require [bill.build :as build]
            [classlojure.core :as classlojure]
            [clojure.java.io :as java-io]
            [clojure.string :as string]))

(def user-directory (java-io/file (System/getProperty "user.home")))

(def maven-directory (java-io/file user-directory ".m2"))

(def maven-repository-directory (java-io/file maven-directory "repository"))

(def bill-directory (java-io/file user-directory ".bill"))

(def bill-repository-directory (java-io/file bill-directory "repository"))

(defn maven-group-directory [{ :keys [group] }]
  (when group
    (reduce java-io/file maven-repository-directory (string/split (name group) #"\."))))
  
(defn maven-artifact-directory [{ :keys [artifact] :as dependency-map }]
  (when artifact
    (when-let [group-directory (maven-group-directory dependency-map)]
      (java-io/file group-directory (name artifact)))))
  
(defn maven-version-directory [{ :keys [version] :as dependency-map }]
  (when version
    (when-let [artifact-directory (maven-artifact-directory dependency-map)]
      (java-io/file artifact-directory (name version)))))

(defn maven-file-name [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (str (name artifact) "-" (name version))))
      
(defn maven-jar [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [version-directory (maven-version-directory dependency-map)]
      (java-io/file version-directory (str (maven-file-name dependency-map) ".jar")))))
      
(defn maven-jar? [dependency-map]
  (when-let [maven-jar-file (maven-jar dependency-map)]
    (.exists maven-jar-file)))

(defn maven-pom [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [version-directory (maven-version-directory dependency-map)]
      (java-io/file version-directory (str (maven-file-name dependency-map) ".pom")))))
      
(defn maven-pom? [dependency-map]
  (when-let [maven-pom-file (maven-pom dependency-map)]
    (.exists maven-pom-file)))
      
(defn bill-algorithm-directory [{ :keys [algorithm] :as dependency-map }]
  (when algorithm
    (java-io/file bill-repository-directory (name algorithm))))

(defn bill-hash-directory [{ :keys [hash] :as dependency-map }]
  (when hash
    (when-let [algorithm-directory (bill-algorithm-directory dependency-map)]
      (java-io/file algorithm-directory (name hash)))))

(defn bill-jar [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [hash-directory (bill-hash-directory dependency-map)]
      (java-io/file hash-directory (str (maven-file-name dependency-map) ".jar")))))
      
(defn bill-jar? [dependency-map]
  (when-let [bill-jar-file (bill-jar dependency-map)]
    (.exists bill-jar-file)))

(defn parse-hash-vector [hash-vector]
  (cond
    (second hash-vector) { :algorithm (first hash-vector) :hash (second hash-vector) }
    (first hash-vector) { :algorithm "SHA-1" :hash (first hash-vector) }))

(defn parse-dependency-symbol [dependency-symbol]
  (when dependency-symbol
    (let [artifact (name dependency-symbol)]
      { :group (or (namespace dependency-symbol) artifact)
        :artifact artifact })))
    
(defn dependency-map [dependency-vector]
  (when dependency-vector
    (let [first-item (first dependency-vector)]
      (if (symbol? first-item)
        (merge
          (parse-dependency-symbol first-item)
          { :version (second dependency-vector) }
          (parse-hash-vector (drop 2 dependency-vector)))
        (parse-hash-vector dependency-vector)))))
            
(defn classpath []
  [])

(defn classloader []
  (apply classlojure/classlojure (classpath)))