(ns bill.tasks.test.jar
  (:use clojure.test
        bill.tasks.jar)
  (:require [bill.task :as task]))

(def manifest-string-value (str "Manifest-Version: 1.0\nCreated-By: " (get (default-manifest) "Created-By") "\nBuilt-By: " (get (default-manifest) "Built-By") "\nBuild-Jdk: " (get (default-manifest) "Build-Jdk")))

(deftest test-init
  (is (task/find-task :jar)))

(deftest test-manifest-map
  (is (= (manifest-map) (default-manifest))))

(deftest test-manifest-string
  (is (= (manifest-string) manifest-string-value)))

(deftest test-manifest-bytes
  (let [manifest-bytes (manifest-bytes)
        manifest-bytes-value (.getBytes manifest-string-value "UTF-8")]
    (is (= (count manifest-bytes) (count manifest-bytes-value)))
    (when (= (count manifest-bytes) (count manifest-bytes-value))
      (doseq [byte-pair (map list manifest-bytes manifest-bytes-value)]
        (is (= (first byte-pair) (second byte-pair)))))))