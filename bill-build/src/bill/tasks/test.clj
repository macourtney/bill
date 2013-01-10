(ns bill.tasks.test
  (:use bill.task)
  (:require [bill.build :as build]
            [clojure.test :as test]
            [clojure.tools.namespace.find :as namespace-find]))

(defn all-test-namespaces []
  (sort (mapcat namespace-find/find-namespaces-in-dir (build/test-path-files))))

(defn parse-namespaces [args]
  (or (map symbol args) (all-test-namespaces)))

(defn load-namespaces [namespaces]
  (apply require :reload namespaces))

(deftask test [& args]
  (let [namespaces (parse-namespaces args)]
    (println "Running tests:" namespaces)
    (load-namespaces namespaces)
    (let [test-results (apply test/run-tests namespaces)]
      (println "test-results:" test-results))))