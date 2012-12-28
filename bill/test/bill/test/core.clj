(ns bill.test.core
  (:use clojure.test
        bill.core)
  (:require [bill.build :as build]
            [bill.repository :as repository]
            [bill.target :as target]
            [bill.util :as util]))

(def project-name 'org.bill/bill)
(def project-version "0.0.1-SNAPSHOT")
(def description "A test build")

(deftest test-defbuild
  (is (repository/bill-jar? (util/dependency-map bill-dependency)))
  (let [old-build (build/build)]
    (is (not (target/find-target :build-test)))

    (defbuild
      { :project [org.bill/bill "0.0.1-SNAPSHOT"]
        :description "A test build" }

      (use 'clojure.test)
      (require ['bill.build :as 'build])

      (is (build/build))

      (deftarget args-test
        "Tests the given arguments against [\"Args\" \"test.\"]."
        [& args]
        (is (= args ["Args" "test."])))

      (build-environment
        (use 'clojure.test)

        (deftarget build-test
          "Tests the given arguments against [\"Args\" \"test.\"]."
          [& args]
          (is (= args ["Args" "test."])))))

    (is (= (merge build/build-defaults { :description description :project ['org.bill/bill project-version] })
           (build/build)))
    (is (classloader))
    (run-target-in-classloader :args-test ["Args" "test."])
    (is (target/find-target :build-test))
    (classloader! nil)
    (build/build! old-build)))