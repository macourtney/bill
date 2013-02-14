(ns bill.test.init-tasks
  (:use clojure.test)
  (:require [bill.build :as build]
            [bill.classloader :as classloader]
            [bill.task :as task]))

(def tasks [:clean :deps :install :install-file :install-maven])

(deftest test-init
  (require 'bill.init-tasks :reload)
  (doseq [task-name tasks]
    (is (task/find-task task-name))))