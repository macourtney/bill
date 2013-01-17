(ns bill.tasks.test.test
  (:use clojure.test
        bill.tasks.test)
  (:require [bill.build :as build]
            [bill.task :as task]
            [clojure.java.io :as java-io]))

(def test-file-list
  [(java-io/file "test/bill/tasks/test/test.clj")
   (java-io/file "test/bill/test/build.clj")
   (java-io/file "test/bill/test/classpath.clj")
   (java-io/file "test/bill/test/maven_repository.clj")
   (java-io/file "test/bill/test/repository.clj")
   (java-io/file "test/bill/test/task.clj")
   (java-io/file "test/bill/test/util.clj")
   (java-io/file "test/bill/test/xml.clj")
   (java-io/file "test/test_utils.clj")])

(deftest test-all-test-namespaces
  (let [saved-build (build/build)]
    (build/build! { :test-paths ["resources"] })
    (is (= (all-test-files) []))
    (build/build! saved-build)))

(deftest test-test-files
  (is (= (test-files (java-io/file "test")) test-file-list)))
    
(deftest test-all-test-files
  (is (= (all-test-files) test-file-list)))
    
(deftest test-parse-namespaces
  (is (= (file-namespaces [] []) []))
  (is (= (file-namespaces [] [(java-io/file "test/bill/tasks/test/test.clj")]) ['bill.tasks.test.test]))
  (is (= (file-namespaces ["bill.tasks.test.test"] []) ['bill.tasks.test.test])))

(deftest test-init
  (is (task/find-task :test))
  (task/remove-task :test)
  (is (nil? (task/find-task :test))))