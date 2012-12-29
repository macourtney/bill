(ns bill.tasks.install-maven
  (:use bill.task)
  (:require [bill.maven-repository :as maven-repository]
            [bill.repository :as repository]
            [bill.util :as util]
            [clojure.java.io :as java-io]
            [clojure.tools.cli :as cli :only [cli]])
  (:import [java.io PushbackReader StringWriter]))

(defn parse-args [args]
  (cli/cli args
     ["-g" "--group" "The group id."]
     ["-a" "--artifact" "The artifact id."]
     ["-v" "--version" "The version."]
     ["-l" "--algorithm" "The hash algorithm to use." :default util/default-algorithm]))

(defn find-file-path [options args]
  (or (:file options) (first args)))

(defn create-file [options args]
  (when-let [file-path (find-file-path options args)]
    (java-io/file file-path)))

(defn find-artifact [options]
  (:artifact options))

(defn find-group [options]
  (or (:group options) (find-artifact options)))

(defn find-version [options]
  (:version options))

(defn find-algorithm [options]
  (:algorithm options))

(defn find-dependencies [options]
  (:dependencies options))

(defn find-dependencies-vector [options]
  (when-let [dependencies (find-dependencies options)]
    (read-string dependencies)))

(defn validate-artifact [options]
  (when-let [artifact (find-artifact options)]
    (and artifact (not-empty artifact))))

(defn validate-version [options]
  (when-let [version (find-version options)]
    (and version (not-empty version))))

(defn validate-args [options]
  (and (validate-artifact options) (validate-version options)))

(defn create-hash
  ([options args] (create-hash options args (find-algorithm options)))
  ([options args algorithm]
    (when-let [file (create-file options args)]
      (util/hash-code file algorithm))))

(defn options-bill-clj-map [options]
  { :group (find-group options)
    :artifact (find-artifact options)
    :version (find-version options) })

(defn create-bill-clj-map [options]
  (maven-repository/convert-to-bill-clj (assoc options :group (find-group options))))

(defn bill-clj-algorithm [options bill-clj-map]
  (or (:algorithm (:file bill-clj-map)) (find-algorithm options) util/default-algorithm))

(defn create-dependency-map [options]
  (when-let [bill-clj-map (create-bill-clj-map options)]
    (let [algorithm (bill-clj-algorithm options bill-clj-map)]
      (merge
        (select-keys bill-clj-map [:group :artifact :version])
        { :algorithm algorithm
          :hash (util/form-hash-code bill-clj-map algorithm) }))))

(defn write-bill-clj [dependency-map]
  (when-let [bill-clj (repository/bill-clj dependency-map)]
    (.mkdirs (.getParentFile bill-clj))
    (java-io/copy (util/form-bytes (create-bill-clj-map dependency-map)) bill-clj)))

(defn copy-bill-jar [options dependency-map]
  (when-let [file (maven-repository/maven-jar dependency-map)]
    (when-let [bill-jar (repository/bill-jar dependency-map)]
      (.mkdirs (.getParentFile bill-jar))
      (java-io/copy file bill-jar))))

(defn print-results [dependency-map]
  (println "Installed:" (util/dependency-vector-str dependency-map)))

(declare update-repository)

(defn install-dependency [dependency-map]
  (when-not (repository/bill-clj? dependency-map)
    (update-repository dependency-map)
    (if-not (repository/bill-clj? dependency-map)
      (println "Could not install:" (util/dependency-vector-str dependency-map))
      true)))

(defn install-dependencies [dependency-map]
  (let [bill-clj (create-bill-clj-map dependency-map)]
    (doseq [dependency-dependency-map (map util/dependency-map (:dependencies bill-clj))]
      (install-dependency dependency-dependency-map))))

(defn update-repository [options]
  (let [dependency-map (create-dependency-map options)]
    (install-dependencies dependency-map)
    (copy-bill-jar options dependency-map)
    (write-bill-clj dependency-map)
    (print-results dependency-map)))

(deftask install-maven [& args]
  (let [[options args banner] (parse-args args)]
    (if (validate-args options)
      (update-repository options)
      (println banner))))