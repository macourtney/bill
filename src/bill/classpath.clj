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

(defn maven-pom [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [version-directory (maven-version-directory dependency-map)]
      (java-io/file version-directory (str (maven-file-name dependency-map) ".pom")))))
      
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
            
(defn classpath []
  [])

(defn classloader []
  (apply classlojure/classlojure (classpath)))