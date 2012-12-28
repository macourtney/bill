(ns bill.maven-repository
  (:require [bill.build :as build]
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
      (java-io/file version-directory (str (util/file-name dependency-map) ".jar")))))

(defn maven-jar? [dependency-map]
  (when-let [maven-jar-file (maven-jar dependency-map)]
    (.exists maven-jar-file)))

(defn maven-pom [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [version-directory (maven-version-directory dependency-map)]
      (java-io/file version-directory (str (util/file-name dependency-map) ".pom")))))

(defn maven-pom? [dependency-map]
  (when-let [maven-pom-file (maven-pom dependency-map)]
    (.exists maven-pom-file)))

(defn project-element [dependency-map]
  (xml/parse (maven-pom dependency-map)))

(defn pom-group [pom-element]
  (first (xml/text (xml/find-child pom-element :groupId))))

(defn pom-parent-group [pom-element]
  (pom-group (xml/find-child pom-element :parent)))

(defn pom-artifact [pom-element]
  (first (xml/text (xml/find-child pom-element :artifactId))))

(defn pom-version [pom-element]
  (first (xml/text (xml/find-child pom-element :version))))

(defn add-jar [dependency-map algorithm]
  (let [algorithm (or algorithm util/default-algorithm)]
    (assoc dependency-map :file { :name (str (util/file-name dependency-map) ".jar")
                                  :algorithm algorithm
                                  :hash (util/hash-code (maven-jar dependency-map) algorithm) })))

(declare convert-to-bill-clj)

(defn pom-dependency [dependency-element algorithm]
  (let [dependency-map { :group (pom-group dependency-element)
                         :artifact (pom-artifact dependency-element)
                         :version (pom-version dependency-element) }
        bill-clj (convert-to-bill-clj dependency-map)
        hash-code (util/form-hash-code bill-clj algorithm)]
    (util/dependency-vector (merge dependency-map { :algorithm algorithm :hash hash-code }))))

(defn pom-dependencies [pom-element algorithm]
  (map #(pom-dependency % algorithm) (xml/find-children (xml/find-child pom-element :dependencies) :dependency)))

(defn convert-to-bill-clj [dependency-map]
  (when-let [pom-element (project-element dependency-map)]
    (let [artifact (pom-artifact pom-element)
          version (pom-version pom-element)
          algorithm (or (:algorithm dependency-map) util/default-algorithm)
          bill-clj-map (add-jar { :group (or (pom-group pom-element) (pom-parent-group pom-element))
                                  :artifact artifact
                                  :version version }
                                algorithm)]
      (assoc bill-clj-map :dependencies (pom-dependencies pom-element algorithm)))))