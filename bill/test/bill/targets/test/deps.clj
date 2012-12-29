(ns bill.targets.test.deps
  (:use clojure.test
        bill.targets.deps)
  (:require [bill.task :as task]))

(deftest test-init
  (is (task/find-task :deps)))