(ns bill.core
  (:require [bill.build :as build]
            [bill.classpath :as classpath]
            [classlojure.core :as classlojure]))

(def classloader-atom (atom nil))

(defn classloader []
  @classloader-atom)
  
(defn classloader! [classloader]
  (reset! classloader-atom classloader))

(defn classloader []
  (apply classlojure/classlojure (classpath/classpath)))
  
(defn execute-build [build-map args]
  (build/build! build-map)
  (let [classloader (classloader)]
    (classlojure/eval-in classloader `(do
                                        (require 'bill.build)
                                        (bill.build/build! '~build-map)))
    (doseq [form args]
      (classlojure/eval-in classloader form)
    (classloader! classloader))))

(defmacro defbuild [build-map & args]
  `(execute-build '~build-map '~args))