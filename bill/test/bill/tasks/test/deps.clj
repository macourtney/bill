(ns bill.tasks.test.deps
  (:use clojure.test)
  (:require [bill.task :as task]))

(deftest test-init
  (require 'bill.tasks.deps :reload)
  (is (task/find-task :deps)))