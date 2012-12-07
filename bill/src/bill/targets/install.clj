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
  (let [algorithm (build/hash-algorithm)]
    { :hash (util/hash-code (find-jar-file) algorithm)
      :algorithm algorithm
      :group (build/group-id)
      :artifact (build/artifact-id)
      :version (build/version)
      :dependencies (build/dependencies) }))
  
(defn install-jar [jar-file]
  (let [clj-map (bill-clj-map)
        repository-bill-jar (repository/bill-jar clj-map)]
    (.mkdirs (.getParentFile repository-bill-jar))
    (java-io/copy (find-jar-file) repository-bill-jar)
    (repository/write-bill-clj (repository/bill-clj clj-map) clj-map)
    (println "Installed as:" (classpath/dependency-vector-str clj-map))))

(deftarget install [& args]
  (if-let [jar-file (find-jar-file)]
    (install-jar jar-file)
    (println "The jar file could not be found. Nothing installed.")))