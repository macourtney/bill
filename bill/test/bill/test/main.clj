(ns bill.test.main
  (:use clojure.test
        bill.main)
  (:require [bill.task :as task]))

(task/deftask noop [& args]) ; No op task for testing.

(deftest test-run-task
  (run-task ["noop"]))