(ns bill.test.init-targets
  (:use clojure.test
        bill.init-targets)
  (:require [bill.task :as task]))

(def tasks [:deps :install :install-file :install-maven])

(deftest test-init
  (doseq [task-name tasks]
    (is (task/find-task task-name))))