(ns bill.tasks.test
  (:use bill.task)
  (:require [bill.build :as build]
            [bill.task :as task]
            [clojure.test :as test]
            [clojure.tools.namespace.file :as namespace-file]
            [clojure.tools.namespace.find :as namespace-find]))

(defn test-files [dir]
  (namespace-find/find-clojure-sources-in-dir dir))

(defn all-test-files []
  (sort (mapcat test-files (build/test-path-files))))
  
(defn file-namespaces [args files]
  (or (seq (map symbol args)) (map second (keep namespace-file/read-file-ns-decl files))))

(defn load-files [test-files]
  (doseq [test-file test-files]
    (load-file (.getAbsolutePath test-file))))

(deftask test
  "Run the project's tests."
  [& args]
  (let [test-files (all-test-files)
        namespaces (file-namespaces args test-files)]
    (println "Running tests:" namespaces)
    (load-files test-files)
    (let [test-results (apply test/run-tests namespaces)]
      (when (> (+ (:error test-results) (:fail test-results)) 0)
        (fail "You have test failures!")))))