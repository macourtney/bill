(ns bill.test.init-tasks
  (:use clojure.test
        bill.init-tasks)
  (:require [bill.task :as task]))

(def tasks [:clean :deps :install :install-file :install-maven])

(deftest test-init
  (doseq [task-name tasks]
    (is (task/find-task task-name))))