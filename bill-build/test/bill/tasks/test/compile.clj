(ns bill.tasks.test.compile
  (:use clojure.test
        bill.tasks.compile)
  (:require [bill.build :as build]
            [bill.task :as task]
            [clojure.java.io :as java-io]))

(def test-fail-exception-namespace "bill.task-fail-exception")
(def test-build-map (merge build/build-defaults
                      { :compile-path "target/classes"
                        :compile [(symbol test-fail-exception-namespace)] }))

(defn build-fixture [test]
  (build/build! test-build-map)
  (test)
  (build/build! build/build-defaults))

(use-fixtures :once build-fixture)

(deftest test-compile?
  (is (compile? 'test 'test))
  (is (not (compile? 'fail 'test)))
  (is (not (compile? 'test nil)))
  (is (not (compile? nil nil)))
  (is (compile? (symbol test-fail-exception-namespace)))
  (is (not (compile? 'fail)))
  (is (not (compile? nil))))

(deftest test-compilable-namespaces
  (is (= (compilable-namespaces) [(symbol test-fail-exception-namespace)])))

(deftest test-path-for
  (is (= (path-for 'test) "test.clj"))
  (is (= (path-for 'bill.test) "bill/test.clj"))
  (is (nil? (path-for nil))))

(deftest test-source-file
  (is (= (source-file (java-io/file "bill") 'test) (java-io/file "bill/test.clj"))))

(deftest test-find-clojure-file
  (is (= (find-clojure-file (symbol test-fail-exception-namespace))
         (java-io/file (first (build/source-path-files)) "bill/task_fail_exception.clj")))
  (is (not (find-clojure-file (symbol "fail")))))

(deftest test-compiled-file
  (is (= (compiled-file (symbol test-fail-exception-namespace))
         (java-io/file (build/compile-path-file) "bill/task_fail_exception__init.class"))))

(deftest test-find-compiled-file
  (is (not (find-compiled-file (symbol "fail")))))
  
(deftest test-stale?
  (is (stale? (symbol test-fail-exception-namespace)))
  (is (not (stale? (symbol "fail")))))

(deftest test-stale-namespaces
  (is (= (stale-namespaces) [(symbol test-fail-exception-namespace)])))

(deftest test-compile-namespaces
  (is (not (find-compiled-file (symbol test-fail-exception-namespace))))
  (compile-namespaces [(symbol test-fail-exception-namespace)])
  (let [compiled-file (find-compiled-file (symbol test-fail-exception-namespace))]
    (is compiled-file)
    (when (and compiled-file (.exists compiled-file))
      (.delete compiled-file))))

(deftest test-compile
  (is (not (find-compiled-file (symbol test-fail-exception-namespace))))
  (task/run-task :compile [])
  (let [compiled-file (find-compiled-file (symbol test-fail-exception-namespace))]
    (is compiled-file)
    (when (and compiled-file (.exists compiled-file))
      (.delete compiled-file))))