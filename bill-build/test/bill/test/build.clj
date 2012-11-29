(ns bill.test.build
  (:use clojure.test
        bill.build))

(deftest test-project
  (let [project-vector [org.bill/bill "0.0.1-SNAPSHOT"]]
    (build! { :project project-vector })
    (is (= (project) project-vector))
    (build! {})))
    
(deftest test-dependencies
  (let [dependencies-vector [[org.clojure/clojure "1.4.0" "SHA-1" "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"]]]
    (build! { :dependencies dependencies-vector })
    (is (= (dependencies) dependencies-vector))
    (build! {})))