(ns bill.core
  (:require [bill.build :as build]
            [bill.bill-environment :as bill-environment]
            [bill.classloader :as classloader]
            [bill.classpath :as classpath]))

(def bill-version "0.0.1-SNAPSHOT")

(System/setProperty "bill.version" bill-version)

(def bill-dependency ['org.bill/bill-build "0.0.1-SNAPSHOT" "SHA-1" "2640ad2a2d2f3ee9070e503a22d8e7c68f6c3684"])

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