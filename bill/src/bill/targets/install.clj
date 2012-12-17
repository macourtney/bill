(ns bill.targets.install
  (:use bill.target)
  (:require [bill.build :as build]
            [bill.classpath :as classpath]
            [bill.repository :as repository]
            [bill.util :as util]
            [clojure.java.io :as java-io]))

(defn find-jar-file []
  (when-let [jar-file (build/target-jar-file)]
    (when (.exists jar-file)
      jar-file)))

(defn bill-clj-map []
  (when-let [jar-file (find-jar-file)]
    (let [algorithm (build/hash-algorithm)]
      { :group (build/group-id)
        :artifact (build/artifact-id)
        :version (build/version)
        :dependencies (build/dependencies)
        :file { :name (.getName jar-file)
                :hash (util/hash-code jar-file algorithm)
                :algorithm algorithm } })))

(defn create-target-bill-clj [clj-map]
  (let [target-bill-clj (repository/target-bill-clj clj-map)]
    (.mkdirs (.getParentFile target-bill-clj))
    (repository/write-bill-clj target-bill-clj clj-map)
    target-bill-clj))

(defn create-repository-jar [dependency-map]
  (let [repository-bill-jar (repository/bill-jar dependency-map)]
    (.mkdirs (.getParentFile repository-bill-jar))
    (java-io/copy (find-jar-file) repository-bill-jar)))

(defn install-jar [jar-file]
  (let [clj-map (bill-clj-map)
        target-bill-clj (create-target-bill-clj clj-map)
        dependency-map (repository/target-bill-clj-dependency-map clj-map)]
    (create-repository-jar dependency-map)
    (java-io/copy target-bill-clj (repository/bill-clj dependency-map))
    (println "Installed as:" (util/serialize-clj (repository/target-bill-clj-dependency-vector clj-map)))))

(deftarget install [& args]
  (if-let [jar-file (find-jar-file)]
    (install-jar jar-file)
    (println "The jar file could not be found. Nothing installed.")))