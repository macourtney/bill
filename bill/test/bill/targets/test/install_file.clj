(ns bill.targets.test.install-file
  (:use clojure.test
        bill.targets.install-file)
  (:require [bill.build :as build]
            [bill.classpath :as classpath]
            [bill.repository :as repository]
            [bill.target :as target]))

(def test-jar-path "resources/test-1.0.0.jar")
(def usage "Usage:\n\n Switches            Default  Desc                          \n --------            -------  ----                          \n -f, --file                   The file to load              \n -g, --group                  The group id.                 \n -a, --artifact               The artifact id.              \n -v, --version                The version.                  \n -l, --algorithm     SHA-1    The hash algorithm to use.    \n -d, --dependencies           The dependency vector to use. \n")
(def algorithm "SHA-1")
(def md5-algorithm "MD5")
(def group "org.test")
(def artifact "test")
(def version "1.0.0")
(def hash-code "da1fbf7e57c838f2a9412778ee97b833c53d9137")
(def md5-hash-code "975d8e1e430f07527155dd40e5c783d8")
(def dependencies-vector [['org.clojure/clojure "1.4.0" "SHA-1" "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"]])
(def dependencies (classpath/serialize-clj dependencies-vector))
            
(deftest test-parse-args
  (is (= (parse-args ["-f" test-jar-path]) [{ :file test-jar-path :algorithm algorithm } [] usage]))
  (is (= (parse-args ["-g" group]) [{ :group group :algorithm algorithm } [] usage]))
  (is (= (parse-args ["--artifact" artifact]) [{ :artifact artifact :algorithm algorithm } [] usage]))
  (is (= (parse-args ["-v" version]) [{ :version version :algorithm algorithm } [] usage]))
  (is (= (parse-args ["-l" md5-algorithm]) [{ :algorithm md5-algorithm } [] usage]))
  (is (= (parse-args ["-d" dependencies]) [{ :dependencies dependencies :algorithm algorithm } [] usage]))
  (is (= (parse-args ["-g" group "-a" artifact "-v" version "--algorithm" md5-algorithm "-d" dependencies test-jar-path])
          [{ :group group :artifact artifact :version version :algorithm md5-algorithm :dependencies dependencies } [test-jar-path] usage]))
  (is (= (parse-args [test-jar-path]) [{ :algorithm algorithm } [test-jar-path] usage]))
  (is (= (parse-args []) [{ :algorithm algorithm } [] usage])))

(deftest test-validate-file
  (is (validate-file { :file test-jar-path } []))
  (is (validate-file {} [test-jar-path]))
  (is (not (validate-file { :file "fail" } [])))
  (is (not (validate-file {} ["fail"])))
  (is (not (validate-file {} []))))

(deftest test-validate-artifact
  (is (validate-artifact { :artifact artifact }))
  (is (not (validate-artifact {}))))

(deftest test-validate-version
  (is (validate-version { :version version }))
  (is (not (validate-version {}))))

(deftest test-validate-args
  (is (validate-args { :artifact artifact :version version } [test-jar-path]))
  (is (not (validate-args { :artifact artifact :version version } [])))
  (is (not (validate-args { :artifact artifact } [test-jar-path])))
  (is (not (validate-args { :version version } [test-jar-path])))
  (is (not (validate-args {} []))))

(deftest test-create-dependency-map
  (is (= (create-dependency-map { :file test-jar-path :group group :artifact artifact :version version :algorithm md5-algorithm } [])
          { :group group :artifact artifact :version version :algorithm md5-algorithm :hash md5-hash-code }))
  (is (= (create-dependency-map { :artifact artifact :version version :algorithm algorithm } [test-jar-path])
          { :group artifact :artifact artifact :version version :algorithm algorithm :hash hash-code })))



(deftest test-create-bill-clj-map
  (is (= (create-bill-clj-map { :file test-jar-path :group group :artifact artifact :version version :algorithm md5-algorithm :dependencies dependencies } [])
          { :group group :artifact artifact :version version :algorithm md5-algorithm :hash md5-hash-code :dependencies dependencies-vector }))
  (is (= (create-bill-clj-map { :artifact artifact :version version :algorithm algorithm :dependencies dependencies } [test-jar-path])
          { :group artifact :artifact artifact :version version :algorithm algorithm :hash hash-code :dependencies dependencies-vector })))

(deftest test-update-repository
  (let [options { :artifact artifact :version version :algorithm algorithm :dependencies dependencies }
        args [test-jar-path]
        dependency-map (create-dependency-map options args)
        hash-directory (repository/bill-hash-directory dependency-map)
        bill-jar-file (repository/bill-jar dependency-map)
        bill-clj-file (repository/bill-clj dependency-map)]
    (try
      (is (not (.exists bill-clj-file)))
      (is (not (.exists bill-jar-file)))
      (is (not (.exists hash-directory)))
      (update-repository options args)
      (is (.exists hash-directory))
      (is (.exists bill-jar-file))
      (is (.exists bill-clj-file))
      (finally
        (.delete bill-clj-file)
        (.delete bill-jar-file)
        (.delete hash-directory)))))