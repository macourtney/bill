(ns bill.core
  (:require [bill.build :as build]
            [bill.build-environment :as build-environment]
            [bill.classpath :as classpath]
            [bill.init-tasks :as init-tasks] ; Loads default tasks.
            [classlojure.core :as classlojure]))

(def classloader-atom (atom nil))

(def bill-dependency ['org.bill/bill-build "0.0.1-SNAPSHOT" "SHA-1" "c00cb8a17eae801a7125e59caab4ce71432f086a"])

(defn classloader []
  @classloader-atom)

(defn classloader! [classloader]
  (reset! classloader-atom classloader))

(defn create-classloader []
  (let [classpath (classpath/build-environment-classpath [bill-dependency])]
    (println "classpath:" classpath)
    (apply classlojure/classlojure classpath)))

(defn run-task-in-classloader [task args]
  (classlojure/eval-in (classloader) `(~'run-task ~task ~args)))

(defn initialize-environments [build-map]
  (classlojure/eval-in (classloader) `(do
                                        (use 'bill.task)
                                        (require 'bill.build)
                                        (require 'bill.init-build-environment-tasks)
                                        (bill.build/update-build! '~build-map)))
  (build-environment/eval-in-build ['(use 'bill.task)]))

(defn eval-forms [forms]
  (doseq [form forms]
    (if (= (first form) 'build-environment)
      (build-environment/eval-in-build (rest form))
      (classlojure/eval-in (classloader) form))))

(defn execute-build [build-map forms]
  (build/update-build! build-map)
  (let [classloader (create-classloader)]
    (classloader! classloader)
    (initialize-environments build-map)
    (eval-forms forms)))

(defmacro defbuild [build-map & args]
  `(execute-build '~build-map '~args))