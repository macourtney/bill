(ns bill.core
  (:require [bill.build :as build]
            [bill.build-environment :as build-environment]
            [bill.classpath :as classpath]
            [bill.init-targets :as init-targets] ; Loads default targets.
            [classlojure.core :as classlojure]))

(def classloader-atom (atom nil))

(def bill-dependency ['org.bill/bill-build "0.0.1-SNAPSHOT" "SHA-1" "c840ba4bb2874cb40109fb536e3e20f348e32725"])

(defn classloader []
  @classloader-atom)
  
(defn classloader! [classloader]
  (reset! classloader-atom classloader))

(defn create-classloader []
  (apply classlojure/classlojure (classpath/classpath [bill-dependency])))
  
(defn run-target-in-classloader [target args]
  (classlojure/eval-in (classloader) `(~'run-target ~target ~args)))

(defn initialize-environments [build-map]
  (classlojure/eval-in (classloader) `(do
                                        (use 'bill.target)
                                        (require 'bill.build)
                                        (bill.build/update-build! '~build-map)))
  (build-environment/eval-in-build ['(use 'bill.target)]))

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