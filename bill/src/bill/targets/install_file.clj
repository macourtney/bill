(ns bill.targets.install-file
  (:use bill.target)
  (:require [bill.classpath :as classpath]
            [bill.repository :as repository]
            [clojure.java.io :as java-io]
            [clojure.tools.cli :as cli :only [cli]])
  (:import [java.io StringWriter]))

(defn parse-args [args]
  (cli/cli args
     ["-f" "--file" "The file to load"] 
     ["-g" "--group" "The group id."]
     ["-a" "--artifact" "The artifact id."]
     ["-v" "--version" "The version."]
     ["-l" "--algorithm" "The hash algorithm to use." :default "SHA-1"]
     ["-d" "--dependencies" "The dependency vector to use."]))

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

(defn validate-args [options args]
  (and (validate-file options args) (validate-artifact options) (validate-version options)))

(defn create-hash [options args]
  (when-let [file (create-file options args)]
    (classpath/hash-code file (find-algorithm options))))
  
(defn create-dependency-map [options args]
  { :group (find-group options)
    :artifact (find-artifact options)
    :version (find-version options)
    :algorithm (find-algorithm options)
    :hash (create-hash options args) })
    
(defn create-bill-clj-map [options args]
  { :group (find-group options)
    :artifact (find-artifact options)
    :version (find-version options)
    :algorithm (find-algorithm options)
    :hash (create-hash options args)
    :dependencies (find-dependencies-vector options) })

(defn write-bill-clj [options args]
  (let [dependency-map (create-dependency-map options args)
        bill-clj (repository/bill-clj dependency-map)]
    (.mkdirs (.getParentFile bill-clj))
    (with-open [bill-clj-writer (java-io/writer bill-clj)]
      (binding [*out* bill-clj-writer]
        (println (classpath/serialize-clj (create-bill-clj-map options args)))))))

(defn copy-bill-jar [options args]
  (let [file (create-file options args)
        dependency-map (create-dependency-map options args)
        bill-jar (repository/bill-jar dependency-map)]
    (.mkdirs (.getParentFile bill-jar))
    (java-io/copy file bill-jar)))
    
(defn print-results [options args]
  (println "Installed as:" (classpath/dependency-vector-str (create-dependency-map options args))))

(defn update-repository [options args]
  (copy-bill-jar options args)
  (write-bill-clj options args)
  (print-results options args))
  
(deftarget install-file [& args]
  (let [[options args banner] (parse-args args)]
    (if (validate-args options args)
      (update-repository options args)
      (println banner))))