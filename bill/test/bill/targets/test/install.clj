(ns bill.targets.test.install
  (:use clojure.test
        bill.targets.install)
  (:require [bill.build :as build]
            [bill.repository :as repository]
            [clojure.java.io :as java-io]))

(def test-jar-name "test-1.0.0.jar")
(def test-jar-path (str "test-resources/" test-jar-name))
(def algorithm "SHA-1")
(def group "org.test")
(def artifact "test")
(def group-artifact (symbol group artifact))
(def version "1.0.0")
(def project-vector [group-artifact version])
(def hash-code "da1fbf7e57c838f2a9412778ee97b833c53d9137")
(def dependencies-vector [['org.clojure/clojure "1.4.0" "SHA-1" "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"]])

(deftest test-find-jar-file
  (let [test-target-jar (java-io/file (build/target-path) test-jar-name)]
    (is (not (find-jar-file)))
    (build/build! (merge build/build-defaults { :project project-vector :dependencies dependencies-vector }))
    (is (not (find-jar-file)))
    (java-io/copy (java-io/file test-jar-path) test-target-jar)
    (try
      (is (find-jar-file))
      (finally
        (.delete test-target-jar)
        (build/build! build/build-defaults)))))

(deftest test-bill-clj-map
  (let [test-target-jar (java-io/file (build/target-path) test-jar-name)]
    (build/build! (merge build/build-defaults { :project project-vector :dependencies dependencies-vector }))
    (java-io/copy (java-io/file test-jar-path) test-target-jar)
    (try
      (is (= (bill-clj-map)
              { :group group
                :artifact artifact
                :version version
                :dependencies dependencies-vector
                :jar { :name test-jar-name
                       :hash hash-code
                       :algorithm algorithm } }))
      (finally
        (.delete test-target-jar)
        (build/build! build/build-defaults)))))

(deftest test-install-jar
  (let [test-target-jar (java-io/file (build/target-path) test-jar-name)]
    (build/build! (merge build/build-defaults { :project project-vector :dependencies dependencies-vector }))
    (java-io/copy (java-io/file test-jar-path) test-target-jar)
    (try
      (let [clj-map (bill-clj-map)
            target-bill-clj (repository/target-bill-clj clj-map)]
        (is (not (.exists target-bill-clj)))
        (install-jar test-target-jar)
        (is (.exists target-bill-clj))
        (let [dependency-map (repository/target-bill-clj-dependency-map clj-map)
              repository-bill-jar (repository/bill-jar dependency-map)
              repository-bill-clj (repository/bill-clj dependency-map)]
          (is (.exists repository-bill-jar))
          (is (.exists repository-bill-clj))
          (.delete repository-bill-jar)
          (.delete repository-bill-clj)
          (.delete (.getParentFile repository-bill-jar))
          (.delete target-bill-clj)
          (is (not (.exists repository-bill-jar)))
          (is (not (.exists repository-bill-clj)))
          (is (not (.exists (.getParentFile repository-bill-jar))))
          (is (not (.exists target-bill-clj)))))
      (finally
        (.delete test-target-jar)
        (build/build! build/build-defaults)))))