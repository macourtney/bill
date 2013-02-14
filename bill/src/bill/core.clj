(ns bill.core
  (:require [bill.build :as build]
            [bill.bill-environment :as bill-environment]
            [bill.classloader :as classloader]
            [bill.classpath :as classpath]
            [bill.init-tasks :as init-tasks])) ; Loads default tasks.

(def bill-version "0.0.1-SNAPSHOT")

(System/setProperty "bill.version" bill-version)

(def bill-dependency ['org.bill/bill-build "0.0.1-SNAPSHOT" "SHA-1" "66cd712aafb57c3ee5b222f376a38da10ea54ec2"])

(defn project-init-form [build-map]
  (let [build-map (assoc build-map :bill-version bill-version)]
    `(do
      (use 'bill.task)
      (require 'bill.build)
      (require 'bill.init-build-environment-tasks)
      (bill.build/update-build! '~build-map))))

(defn initialize-environments [build-map]
  (classloader/eval-in (project-init-form build-map))
  (bill-environment/eval-in ['(use 'bill.task)]))

(defn eval-forms [forms]
  (doseq [form forms]
    (if (= (first form) 'bill-environment)
      (bill-environment/eval-in (rest form))
      (classloader/eval-in form))))

(defn execute-build [build-map forms]
  (build/update-build! build-map)
  (classloader/init-classloader [bill-dependency])
  (initialize-environments build-map)
  (eval-forms forms))

(defmacro defbuild [build-map & args]
  `(execute-build '~build-map '~args))