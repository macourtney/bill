(ns bill.tasks.compile
  (:use bill.task)
  (:require [bill.build :as build]
            [clojure.java.io :as java-io]))

(defn compile?
  ([namespace] (some #(compile? namespace %) (build/compile)))
  ([namespace compile-symbol]
    (when (and namespace compile-symbol)
      (= (name compile-symbol) (name namespace)))))

(defn compilable-namespaces []
  (filter compile? (build/source-namespaces)))

(defn path-for
  "Transform a namespace into a .clj file path relative to classpath root."
  [namespace]
  (when namespace
    (str (.replace (.replace (name namespace) \- \_) \. \/) ".clj")))

(defn source-file [source-dir namespace]
  (java-io/file source-dir (path-for namespace)))

(defn find-clojure-file [namespace]
  (some #(when-let [source-file (source-file % namespace)]
          (when (.exists source-file)
            source-file))
        (build/source-path-files)))

(defn compiled-file [namespace]
  (when namespace
    (java-io/file
      (build/compile-path-file)
      (str (.replace (.replace (name namespace) \- \_) \. \/) "__init.class"))))

(defn find-compiled-file [namespace]
  (when-let [compiled-file (compiled-file namespace)]
    (when (.exists compiled-file)
      compiled-file)))

(defn stale? [namespace]
  (when-let [clojure-file (find-clojure-file namespace)]
    (if-let [compiled-file (find-compiled-file namespace)]
      (when (>= (.lastModified clojure-file) (.lastModified compiled-file))
        namespace)
      namespace)))

(defn stale-namespaces []
  (seq (filter stale? (compilable-namespaces))))

(defn compile-namespaces [namespaces]
  (if namespaces
    (try
      (.mkdirs (build/compile-path-file))
      (binding [*compile-path* (build/compile-path-file)]
        (doseq [namespace namespaces]
          (println "Compiling" namespace)
          (clojure.core/compile namespace)))
      (catch Exception e
        (println "Compilation failed:" (.getMessage e))))
    (println "Nothing to compile.")))

(deftask compile
  "Compiles all clj files which need to be compiled."
  [& args]
  (compile-namespaces (stale-namespaces)))