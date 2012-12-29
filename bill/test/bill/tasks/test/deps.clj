(ns bill.tasks.test.deps
  (:use clojure.test
        bill.tasks.deps)
  (:require [bill.task :as task]))

(deftest test-init
  (is (task/find-task :deps)))