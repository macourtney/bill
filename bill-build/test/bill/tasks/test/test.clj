(ns bill.tasks.test.test
  (:use clojure.test
        bill.tasks.test)
  (:require [bill.task :as task]))

(deftest test-init
  (is (task/find-task :test))
  (task/remove-task :test)
  (is (nil? (task/find-task :test))))