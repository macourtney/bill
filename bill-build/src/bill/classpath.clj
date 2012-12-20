(ns bill.classpath
  (:require [bill.build :as build]
            [bill.repository :as repository]
            [bill.util :as util]
            [clojure.java.io :as java-io]
            [clojure.string :as string])
  (:import [java.io PushbackReader]
           [java.security MessageDigest]))

(defn assure-hash-directory [algorithm hash]
  (let [hash-directory (repository/bill-hash-directory { :algorithm algorithm :hash hash })]
    (when (not (.exists hash-directory))
      (.mkdirs hash-directory))
    hash-directory))

(defn move-to-repository
  ([jar-file] (move-to-repository jar-file util/default-algorithm))
  ([jar-file algorithm]
    (let [file-hash (util/hash-code jar-file algorithm)
          hash-directory (assure-hash-directory algorithm file-hash)]
      (java-io/copy jar-file (java-io/file hash-directory (.getName jar-file))))))

(defn dependencies [dependency-map]
  (when-let [bill-clj-map (repository/read-bill-clj dependency-map)]
    (:dependencies bill-clj-map)))

(defn child-dependency-maps [parent-dependency-map]
  (filter identity (map util/dependency-map (dependencies parent-dependency-map))))

(defn group-artifact-str [dependency-map]
  (when dependency-map
    (when-let [artifact (:artifact dependency-map)]
      (str (name (or (:group dependency-map) artifact)) "/" (name artifact)))))

(defn create-classpath
  ([] (create-classpath
        { :classpath {} 
          :dependencies (map util/dependency-map (build/dependencies)) }))
  ([classpath-map]
    (let [classpath-dependencies (:dependencies classpath-map)]
      (if-let [dependency-map (first classpath-dependencies)]
        (let [group-artifact (group-artifact-str dependency-map)
              classpath (:classpath classpath-map)
              next-dependencies (rest classpath-dependencies)]
          (if (contains? classpath group-artifact)
            (recur (assoc classpath-map :dependencies next-dependencies))
            (recur { :classpath (assoc classpath group-artifact dependency-map)
                     :dependencies (if-let [child-dependencies (seq (child-dependency-maps dependency-map))]
                                     (if next-dependencies
                                       (concat next-dependencies child-dependencies)
                                       child-dependencies)
                                     next-dependencies) })))
        (:classpath classpath-map)))))
      
(defn resolve-dependencies
  ([] (resolve-dependencies (build/dependencies)))
  ([dependencies]
    (map repository/bill-jar (vals (create-classpath
      { :classpath {} 
        :dependencies (map util/dependency-map dependencies) } )))))
      
(defn classpath [bill-dependencies]
  (resolve-dependencies (concat (build/dependencies) bill-dependencies)))