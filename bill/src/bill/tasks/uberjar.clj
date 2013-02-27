(ns bill.tasks.uberjar
  (:use bill.task)
  (:require [bill.build :as build]
            [bill.classpath :as classpath]
            [clojure.java.io :as java-io])
  (:import [java.io File]
           [java.util.zip ZipEntry ZipFile ZipOutputStream]))

(defn jar?
  "Returns true if the given file is a jar file."
  [^File file]
  (.endsWith (.getName file) ".jar"))

(defn jar-dependencies
  "Returns all of the dependency jars to put in the uberjar."
  []
  (filter jar? (classpath/classpath [])))

(defn jars
  "Returns all of the jars to put in the uberjar."
  []
  (cons (build/target-jar-file) (jar-dependencies)))

(defn uberjar-stream
  "Returns the zip stream of the uberjar."
  []
  (when-let [uberjar (build/target-uberjar-file)]
    (.mkdirs (.getParentFile uberjar))
    (ZipOutputStream. (java-io/output-stream uberjar))))

(defn jar-entries
  "Returns all of the entries in the given jar ZipFile "
  [^ZipFile jar-zip-file]
  (when jar-zip-file
    (enumeration-seq (.entries jar-zip-file))))
  
(defn include-entry?
  "Returns true if the given zip entry is not already included and not excluded by uberjar-exclusions."
  [^ZipEntry entry already-included]
  (when entry
    (let [entry-name (.getName entry)]
      (not
        (or
          (contains? already-included entry-name)
          (some #(re-find % entry-name) (build/uberjar-exclusions)))))))

(defn filtered-jar-entries
  "Returns all of the entries of the given jar excluding the jar exclusions."
  [^ZipFile jar-zip-file already-included]
  (filter #(include-entry? % already-included) (jar-entries jar-zip-file)))

(defn write-entry
  "Writes the given entry from the given jar file to the given uber jar stream."
  [^ZipOutputStream uberjar-stream ^ZipFile jar-zip-file ^ZipEntry entry]
  (when (and uberjar-stream jar-zip-file entry)
    (.setCompressedSize entry -1) ; some jars report size incorrectly
    (.putNextEntry uberjar-stream entry)
    (java-io/copy (.getInputStream jar-zip-file entry) uberjar-stream)
    (.closeEntry uberjar-stream)
    (.getName entry)))

(defn write-jar
  "Writes the given jar to the given uber-jar-stream."
  [^ZipOutputStream uber-jar-stream ^File jar already-included]
  (println "Including" (.getName jar))
  (with-open [jar-zip-file (ZipFile. jar)]
    (reduce #(conj %1 (write-entry uber-jar-stream jar-zip-file %2))
      already-included
      (filtered-jar-entries jar-zip-file already-included))))

(defn write-jars
  "Writes all of the jars to the given uber jar stream."
  [^ZipOutputStream uber-jar-stream]
  (reduce #(write-jar uber-jar-stream %2 %1) #{} (jars)))

(defn write-uberjar
  "Writes the full uberjar."
  []
  (with-open [uberjar-stream (uberjar-stream)]
    (write-jars uberjar-stream))
  (println "Created" (.getAbsolutePath (build/target-uberjar-file))))

(deftask uberjar
  "Package up all the project's files and dependencies into a single jar file."
  [& args]
  (run-task :jar [])
  (write-uberjar))