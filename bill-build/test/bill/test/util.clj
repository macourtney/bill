(ns bill.test.util
  (:use clojure.test
        bill.util))

(deftest test-user-directory
  (is user-directory)
  (is (.exists user-directory)))

(deftest test-serialize-clj
  (is (= (serialize-clj 1) "1"))
  (is (= (serialize-clj "blah") "\"blah\""))
  (is (= (serialize-clj { :foo "bar" }) "#=(clojure.lang.PersistentArrayMap/create {:foo \"bar\"})")))