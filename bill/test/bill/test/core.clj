(ns bill.test.core
  (:use clojure.test
        bill.core)
  (:require [bill.build :as build]
            [bill.classloader :as classloader]
            [bill.repository :as repository]
            [bill.task :as task]
            [bill.util :as util]))

(def project-name 'org.bill/bill)
(def project-version "0.0.1-SNAPSHOT")
(def description "A test build")

(deftest test-defbuild
  (is (repository/bill-jar? (util/dependency-map bill-dependency)))
  (let [old-build (build/build)]
    (is (not (task/find-task :build-test)))

    (binding [build/build-environment? false] ; Force build-environment? to be rebound later.
      (defbuild
        { :project [org.bill/bill "0.0.1-SNAPSHOT"]
          :description "A test build" }

        (use 'clojure.test)
        (require ['bill.build :as 'build])

        (is (build/build))
        (is build/build-environment?)

        (deftask args-test
          "Tests the given arguments against [\"Args\" \"test.\"]."
          [& args]
          (is (= args ["Args" "test."])))

        (bill-environment
          (use 'clojure.test)

          (is (not build/build-environment?))

          (deftask build-test
            "Tests the given arguments against [\"Args\" \"test.\"]."
            [& args]
            (is (= args ["Args" "test."])))))
      
      (is (= (merge build/build-defaults
                    (select-keys old-build [:dependencies :bill-version])
                    { :description description
                     :project ['org.bill/bill project-version]
                     :bill-version "0.0.1-SNAPSHOT" })
             (build/build)))
      (is (classloader/classloader))
      (classloader/run-task-in-classloader :args-test ["Args" "test."])
      (classloader/classloader! nil)
      (is (task/find-task :build-test)))

    (build/build! old-build)))