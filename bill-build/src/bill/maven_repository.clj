(ns bill.maven-repository
  (:require [bill.build :as build]
            [bill.repository :as repository]
            [bill.util :as util]
            [bill.xml :as xml]
            [clojure.java.io :as java-io]
            [clojure.string :as string])
  (:import [java.io PushbackReader]
           [java.security MessageDigest]))

(def maven-directory (java-io/file util/user-directory ".m2"))

(def maven-repository-directory (java-io/file maven-directory "repository"))

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
      
(defn maven-jar [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [version-directory (maven-version-directory dependency-map)]
      (java-io/file version-directory (str (repository/file-name dependency-map) ".jar")))))
      
(defn maven-jar? [dependency-map]
  (when-let [maven-jar-file (maven-jar dependency-map)]
    (.exists maven-jar-file)))

(defn maven-pom [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [version-directory (maven-version-directory dependency-map)]
      (java-io/file version-directory (str (repository/file-name dependency-map) ".pom")))))
      
(defn maven-pom? [dependency-map]
  (when-let [maven-pom-file (maven-pom dependency-map)]
    (.exists maven-pom-file)))

(defn project-element [dependency-map]
  (xml/parse (maven-pom dependency-map)))

(defn convert-to-bill-clj [dependency-map]
  (when-let [pom-element (project-element dependency-map)]
    ))