(ns bill.util
  (:require [clojure.java.io :as java-io])
  (:import [java.io StringWriter]))

(def user-directory (java-io/file (System/getProperty "user.home")))

(defn serialize-clj [form]
  (when form
    (let [string-writer (new StringWriter)]
      (binding [*print-dup* true]
        (binding [*out* string-writer]
          (print form)))
      (.close string-writer)
      (.toString string-writer))))

(defn write-form [file form]
  (with-open [bill-clj-writer (java-io/writer file)]
    (binding [*out* bill-clj-writer]
      (println (serialize-clj form)))))