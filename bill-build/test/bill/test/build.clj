(ns bill.test.build
  (:use clojure.test
        bill.build)
  (:require [clojure.java.io :as java-io])
  (:import [java.io File]))

(def test-group-id-str "org.bill")
(def test-artifact-id-str "bill")
(def test-version-str "0.0.1-SNAPSHOT")
(def test-group-artifact (symbol test-group-id-str test-artifact-id-str))
(def test-project-vector [test-group-artifact test-version-str])

(deftest test-project
  (update-build! { :project test-project-vector })
  (is (= (project) test-project-vector))
  (build! build-defaults))

(deftest test-dependencies
  (let [dependencies-vector [['org.clojure/clojure "1.4.0" "SHA-1" "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"]]]
    (update-build! { :dependencies dependencies-vector })
    (is (= (dependencies) dependencies-vector))
    (build! build-defaults)))

(deftest test-target-path
  (let [target-path-str "test-target/"]
    (update-build! { :target-path target-path-str })
    (is (= (target-path) target-path-str))
    (build! build-defaults)
    (is (= (target-path) (:target-path build-defaults)))))

(deftest test-artifact-id
  (update-build! { :project test-project-vector })
  (is (= (artifact-id) test-artifact-id-str))
  (update-build! { :project ['bill test-version-str] })
  (is (= (artifact-id) test-artifact-id-str))
  (build! build-defaults)
  (is (nil? (artifact-id))))

(deftest test-group-id
  (update-build! { :project test-project-vector })
  (is (= (group-id) test-group-id-str))
  (update-build! { :project ['bill test-version-str] })
  (is (= (group-id) test-artifact-id-str))
  (build! build-defaults)
  (is (nil? (group-id))))

(deftest test-version
  (update-build! { :project test-project-vector })
  (is (= (version) test-version-str))
  (build! build-defaults)
  (is (nil? (version))))

(deftest test-jar-base-name
  (update-build! { :project test-project-vector })
  (is (= (jar-base-name) (str test-artifact-id-str "-" test-version-str)))
  (build! build-defaults)
  (is (nil? (jar-base-name))))

(deftest test-jar-name
  (let [test-jar-name "test-jar-name.jar"]
    (update-build! { :project test-project-vector
                     :jar-name test-jar-name })
    (is (= (jar-name) test-jar-name)))
  (build! build-defaults)
  (update-build! { :project test-project-vector })
  (is (= (jar-name) (str (jar-base-name) ".jar")))
  (build! build-defaults)
  (is (nil? (jar-name))))
  
(deftest test-jar-name-file
  (build! build-defaults)
  (update-build! { :project test-project-vector })
  (let [test-target-jar-file (target-jar-file)]
    (is (instance? File test-target-jar-file))
    (is (= (.getPath test-target-jar-file) (.getPath (java-io/file (target-path) (jar-name))))))
  (build! build-defaults)
  (is (nil? (target-jar-file))))

(deftest test-uberjar-name
  (let [test-uberjar-name "test-uberjar-name.jar"]
    (update-build! { :project test-project-vector
                     :uberjar-name test-uberjar-name })
    (is (= (uberjar-name) test-uberjar-name)))
  (build! build-defaults)
  (update-build! { :project test-project-vector })
  (is (= (uberjar-name) (str (jar-base-name) "-standalone.jar")))
  (build! build-defaults)
  (is (nil? (uberjar-name))))

(deftest test-jar-name-file
  (build! build-defaults)
  (update-build! { :project test-project-vector })
  (let [test-target-uberjar-file (target-uberjar-file)]
    (is (instance? File test-target-uberjar-file))
    (is (= (.getPath test-target-uberjar-file) (.getPath (java-io/file (target-path) (uberjar-name))))))
  (build! build-defaults)
  (is (nil? (target-uberjar-file))))