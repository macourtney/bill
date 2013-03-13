(ns bill.classloader
  (:require [bill.build :as build]
            [bill.classpath :as classpath]
            [classlojure.core :as classlojure]))

(def classloader-atom (atom nil))

(defn classloader []
  @classloader-atom)

(defn classloader! [classloader]
  (reset! classloader-atom classloader))

(defn init-classloader [initial-classpath]
  (let [classpath (classpath/build-environment-classpath initial-classpath)
        classloader (apply classlojure/classlojure classpath)]
    (classloader! classloader)))

(defn eval-in [form]
  (binding [build/build-environment? true]
    (classlojure/eval-in (classloader) form)))

(defn run-task-in-classloader [task args]
  (println "Running task" task "in classloader.")
  (eval-in 
    `(try
      (if (~'bill.task/find-task ~task)
        (~'bill.task/run-task ~task '~args)
        (println "Task not found:" ~task))
      (catch org.bill.TaskFailException task-fail-exception#
        (println "The task failed:" (.getMessage task-fail-exception#))
        (System/exit -1))
      (catch Throwable throwable#
       (.printStackTrace throwable#)
       (System/exit -1)))))