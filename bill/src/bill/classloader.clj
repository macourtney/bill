(ns bill.classloader
  (:require [bill.build :as build]
            [bill.build-environment :as build-environment]
            [bill.classpath :as classpath]
            [classlojure.core :as classlojure]))

(def classloader-atom (atom nil))

(defn classloader []
  @classloader-atom)

(defn classloader! [classloader]
  (reset! classloader-atom classloader))

(defn init-classloader [initial-classpath]
  (let [classpath (classpath/build-environment-classpath initial-classpath)]
    (classloader! (apply classlojure/classlojure classpath))))

(defn eval-in [form]
  (classlojure/eval-in (classloader) form))
    
(defn run-task-in-classloader [task args]
  (eval-in `(~'run-task ~task ~args)))