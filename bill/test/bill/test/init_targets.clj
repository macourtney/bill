(ns bill.test.init-targets
  (:use clojure.test
        bill.init-targets)
  (:require [bill.target :as target]))

(def targets [:deps :install :install-file :install-maven])

(deftest test-init
  (doseq [target-name targets]
    (is (target/find-target target-name))))