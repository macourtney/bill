(ns bill.targets.test.deps
  (:use clojure.test
        bill.targets.deps)
  (:require [bill.target :as target]))

(deftest test-init
  (is (target/find-target :deps)))