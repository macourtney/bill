(ns bill.targets.install-file
  (:use bill.target)
  (:require [bill.classpath :as classpath]
            [bill.repository :as repository]
            [bill.util :as util]
            [clojure.java.io :as java-io]
            [clojure.tools.cli :as cli :only [cli]])
  (:import [java.io PushbackReader StringWriter]))

(defn parse-args [args]
  (cli/cli args
     ["-f" "--file" "The file to load"] 
     ["-g" "--group" "The group id."]
     ["-a" "--artifact" "The artifact id."]
     ["-v" "--version" "The version."]
     ["-l" "--algorithm" "The hash algorithm to use." :default util/default-algorithm]
     ["-d" "--dependencies" "The dependency vector to use."]
     ["-c" "--clj" "The clj file to use."]))

(defn find-file-path [options args]
  (or (:file options) (first args)))

(defn create-file [options args]
  (when-let [file-path (find-file-path options args)]
    (java-io/file file-path)))

(defn file-name [options args]
  (when-let [file (create-file options args)]
    (.getName file)))

(defn find-artifact [options]
  (:artifact options))

(defn find-group [options]
  (or (:group options) (find-artifact options)))

(defn find-version [options]
  (:version options))

(defn find-algorithm [options]
  (:algorithm options))

(defn find-clj [options]
  (:clj options))

(defn find-clj-file [options]
  (when-let [clj-file-name (find-clj options)]
    (java-io/file clj-file-name)))

(defn find-dependencies [options]
  (:dependencies options))

(defn find-dependencies-vector [options]
  (when-let [dependencies (find-dependencies options)]
    (read-string dependencies)))

(defn validate-file [options args]
  (if-let [file (create-file options args)]
    (.exists file)
    false))

(defn validate-artifact [options]
  (when-let [artifact (find-artifact options)]
    (and artifact (not-empty artifact))))

(defn validate-version [options]
  (when-let [version (find-version options)]
    (and version (not-empty version))))

(defn file-bill-clj-map [options]
  (when-let [clj-file (find-clj-file options)]
    (when (.exists clj-file)
      (read (PushbackReader. (java-io/reader clj-file))))))
    
(defn validate-clj [options]
  (when-let [clj-file (find-clj options)]
    (when (not-empty clj-file)
      (when-let [clj-map (file-bill-clj-map options)]
        (and (validate-artifact clj-map) (validate-version clj-map))))))

(defn validate-args [options args]
  (and (validate-file options args) (or (validate-clj options) (and (validate-artifact options) (validate-version options)))))

(defn create-hash
  ([options args] (create-hash options args (find-algorithm options)))
  ([options args algorithm]
    (when-let [file (create-file options args)]
      (util/hash-code file algorithm))))

(defn options-bill-clj-map [options args]
  { :group (find-group options)
    :artifact (find-artifact options)
    :version (find-version options)
    :dependencies (find-dependencies-vector options) })

(defn bill-clj-algorithm [options bill-clj-map]
  (or (:algorithm (:file bill-clj-map)) (find-algorithm options) util/default-algorithm))

(defn update-bill-clj-map-hash [options args bill-clj-map]
  (let [algorithm (bill-clj-algorithm options bill-clj-map)]
    (merge
      (dissoc bill-clj-map :algorithm)
      { :file { :name (file-name options args)
                :algorithm algorithm
                :hash (create-hash options args algorithm) } })))

(defn create-bill-clj-map [options args]
  (update-bill-clj-map-hash options args
    (merge (options-bill-clj-map options args) (file-bill-clj-map options))))

(defn write-temp-bill-clj [options args]
  (when-let [bill-clj-map (create-bill-clj-map options args)]
    (let [temp-bill-clj (repository/temp-bill-clj bill-clj-map)]
      (repository/write-bill-clj temp-bill-clj bill-clj-map)
      temp-bill-clj)))

(defn create-dependency-map [options args]
  (when-let [temp-bill-clj (write-temp-bill-clj options args)]
    (let [bill-clj-map (repository/read-bill-clj-file temp-bill-clj)
          algorithm (bill-clj-algorithm options bill-clj-map)]
      (merge
        (select-keys bill-clj-map [:group :artifact :version])
        { :algorithm algorithm
          :hash (util/hash-code temp-bill-clj algorithm) }))))

(defn write-bill-clj [dependency-map]
  (when-let [bill-clj (repository/bill-clj dependency-map)]
    (.mkdirs (.getParentFile bill-clj))
    (java-io/copy (repository/temp-bill-clj dependency-map) bill-clj)))

(defn copy-bill-jar [options args dependency-map]
  (let [file (create-file options args)]
    (when-let [bill-jar (repository/bill-jar dependency-map)]
      (.mkdirs (.getParentFile bill-jar))
      (java-io/copy file bill-jar))))
    
(defn print-results [dependency-map]
  (println "Installed:" (util/dependency-vector-str dependency-map)))

(defn update-repository [options args]
  (let [dependency-map (create-dependency-map options args)]
    (copy-bill-jar options args dependency-map)
    (write-bill-clj dependency-map)
    (print-results dependency-map)))
  
(deftarget install-file [& args]
  (let [[options args banner] (parse-args args)]
    (if (validate-args options args)
      (update-repository options args)
      (println banner))))