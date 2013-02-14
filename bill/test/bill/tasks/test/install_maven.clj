(ns bill.tasks.test.install-maven
  (:use clojure.test
        bill.tasks.install-maven)
  (:require [bill.repository :as repository]
            [bill.util :as util]))

(def new-line (System/getProperty "line.separator"))

(def usage (str "Usage:" new-line new-line " Switches         Default  Desc                       " new-line " --------         -------  ----                       " new-line " -g, --group               The group id.              " new-line " -a, --artifact            The artifact id.           " new-line " -v, --version             The version.               " new-line " -l, --algorithm  SHA-1    The hash algorithm to use. " new-line))
(def algorithm "SHA-1")
(def md5-algorithm "MD5")
(def group "org.clojure")
(def artifact "clojure")
(def version "1.4.0")
(def hash-code "da1fbf7e57c838f2a9412778ee97b833c53d9137")
(def md5-hash-code "b30938b3f94902dca57638c610a02e07")
(def dependencies-vector [])
(def dependencies (util/serialize-clj dependencies-vector))

(deftest test-parse-args
  (is (= (parse-args ["-g" group]) [{ :group group :algorithm algorithm } [] usage]))
  (is (= (parse-args ["--artifact" artifact]) [{ :artifact artifact :algorithm algorithm } [] usage]))
  (is (= (parse-args ["-v" version]) [{ :version version :algorithm algorithm } [] usage]))
  (is (= (parse-args ["-l" md5-algorithm]) [{ :algorithm md5-algorithm } [] usage]))
  (is (= (parse-args ["-g" group "-a" artifact "-v" version "--algorithm" md5-algorithm])
          [{ :group group :artifact artifact :version version :algorithm md5-algorithm } [] usage]))
  (is (= (parse-args []) [{ :algorithm algorithm } [] usage])))

(deftest test-validate-artifact
  (is (validate-artifact { :artifact artifact }))
  (is (not (validate-artifact {}))))

(deftest test-validate-version
  (is (validate-version { :version version }))
  (is (not (validate-version {}))))

(deftest test-validate-args
  (is (validate-args { :group group :artifact artifact :version version }))
  (is (validate-args { :artifact artifact :version version }))
  (is (not (validate-args { :artifact artifact })))
  (is (not (validate-args { :version version })))
  (is (not (validate-args {}))))

(deftest test-create-dependency-map
  (let [test-dependency-map (create-dependency-map { :group group :artifact artifact :version version :algorithm md5-algorithm })]
    (is (= test-dependency-map
         { :algorithm md5-algorithm :hash (:hash test-dependency-map) :group group :artifact artifact :version version })))
  (let [test-dependency-map (create-dependency-map { :group group :artifact artifact :version version })]
    (is (= test-dependency-map
         { :algorithm algorithm :hash (:hash test-dependency-map) :group group :artifact artifact :version version }))))

(deftest test-create-bill-clj-map
  (is (= (create-bill-clj-map { :group group :artifact artifact :version version :algorithm md5-algorithm :dependencies dependencies })
          { :group group :artifact artifact :version version :file { :name (str artifact "-" version ".jar") :algorithm md5-algorithm :hash md5-hash-code } :dependencies dependencies-vector })))

(deftest test-update-repository
  (let [options { :group group  :artifact artifact :version version :algorithm algorithm }
        args []
        dependency-map (create-dependency-map options)
        hash-directory (repository/bill-hash-directory dependency-map)
        bill-jar-file (repository/bill-jar dependency-map)
        bill-clj-file (repository/bill-clj dependency-map)]
    (try
      (when (and bill-clj-file (.exists bill-clj-file))
        (.delete bill-clj-file))
      (when (and bill-jar-file (.exists bill-jar-file))
        (.delete bill-jar-file))
      (when (and hash-directory (.exists hash-directory))
        (.delete hash-directory))
      (update-repository options)
      (is (when hash-directory (.exists hash-directory)))
      (is (when bill-jar-file (.exists bill-jar-file)))
      (is (when bill-clj-file (.exists bill-clj-file))))))