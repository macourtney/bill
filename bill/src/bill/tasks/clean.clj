(ns bill.tasks.clean
  (:use bill.task)
  (:require [bill.build :as build]
            [clojure.java.io :as java-io])
  (:import [java.io File]))

(defn delete [^File path]
  (when path
    (when-not (.delete path)
      (fail (str "Could not delete: " (.getPath path))))))

(defn recursive-delete [^File path]
  (when (.isDirectory path)
    (doseq [child-path (.listFiles path)]
      (recursive-delete child-path)))
  (delete path))

(deftask clean
  "Delete all files from project's target-path."
  [& args]
  (if-let [target-path (build/target-path)]
    (when-let [target-file (java-io/file target-path)]
      (when (.exists target-file)
        (recursive-delete target-file)))
    (println "No dependencies found.")))