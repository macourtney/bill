(ns bill.core
  (:require [bill.build :as build]
            [bill.classpath :as classpath]
            [classlojure.core :as classlojure]))

(defn execute-build [build-map args]
  (build/build! build-map)
  (let [classloader (classpath/classloader)]
    (classlojure/eval-in classloader `(do
                                        (require 'bill.build)
                                        (bill.build/build! '~build-map)))
    (doseq [form args]
      (classlojure/eval-in classloader form))))

(defmacro defbuild [build-map & args]
  `(execute-build '~build-map '~args))