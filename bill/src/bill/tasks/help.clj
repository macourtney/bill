(ns bill.tasks.help
  (:use bill.task)
  (:require [bill.build :as build]
            [bill.classloader :as classloader]
            [clojure.string :as string])
  (:import [java.io File]
           [java.util Comparator]))

(def task-sorter
  (reify Comparator
    (compare [this task1 task2]
      (.compareTo (task-name task1) (task-name task2)))

    (equals [this task2]
      (.equals (task-name this) (task-name task2)))))
  
(defn sort-tasks [tasks]
  (sort task-sorter tasks))
  
(defn collect-tasks []
  (sort-tasks (vals (tasks))))

(defn create-spaces [task-name]
  (let [number-of-spaces (- 16 (count task-name))]
    (if (pos? number-of-spaces)
      (string/join "" (repeat number-of-spaces " "))
      "")))
  
(defn print-task [task]
  (let [task-name (task-name task)]
    (println task-name (create-spaces task-name) (task-description task))))

(defn print-tasks [tasks]
  (doseq [task tasks]
    (print-task task)))

(deftask help
  "Display a list of tasks."
  [& args]
  (let [tasks (collect-tasks)]
    (println "Bill is a tool for building Clojure projects.")
    (println)
    (println "Several tasks are available:")
    (print-tasks tasks)))