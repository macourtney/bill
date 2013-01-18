(ns bill.core
  (:require [bill.build :as build]
            [bill.build-environment :as build-environment]
            [bill.classloader :as classloader]
            [bill.classpath :as classpath]
            [bill.init-tasks :as init-tasks])) ; Loads default tasks.

(def bill-dependency ['org.bill/bill-build "0.0.1-SNAPSHOT" "SHA-1" "a3c30cec30f8e2841629bc4c708cdb06ac58f600"])

(defn project-init-form [build-map]
  `(do
    (use 'bill.task)
    (require 'bill.build)
    (require 'bill.init-build-environment-tasks)
    (bill.build/update-build! '~build-map)))

(defn initialize-environments [build-map]
  (classloader/eval-in (project-init-form build-map))
  (build-environment/eval-in-build ['(use 'bill.task)]))

(defn eval-forms [forms]
  (doseq [form forms]
    (if (= (first form) 'build-environment)
      (build-environment/eval-in-build (rest form))
      (classloader/eval-in form))))

(defn execute-build [build-map forms]
  (build/update-build! build-map)
  (let [classloader (classloader/init-classloader [bill-dependency])]
    (initialize-environments build-map)
    (eval-forms forms)))

(defmacro defbuild [build-map & args]
  `(execute-build '~build-map '~args))