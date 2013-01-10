(ns bill.tasks.test.test
  (:use clojure.test
        bill.tasks.test)
  (:require [bill.build :as build]
            [bill.task :as task]))

(deftest test-all-test-namespaces
  (let [saved-build (build/build)]
    (build/build! { :test-paths ["resources"] })
    (is (= (all-test-namespaces) []))
    (build/build! saved-build)))

(deftest test-parse-namespaces
  (is (= (parse-namespaces []) []))
  (is (= (parse-namespaces ["bill.tasks.test.test"]) ['bill.tasks.test.test])))

(deftest test-init
  (is (task/find-task :test))
  (task/remove-task :test)
  (is (nil? (task/find-task :test))))