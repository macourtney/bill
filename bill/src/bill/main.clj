(ns bill.main
  (:require [bill.classloader :as classloader]
            [bill.task :as task])
  (:import [org.bill TaskFailException]))

(defn run-task [args]
  (let [task (or (first args) "help")]
    (if (task/find-task task)
      (task/run-task task (rest args))
      (classloader/run-task-in-classloader task (rest args)))))

(defn -main [& args]
  (try
    (load-file "build.clj")
    (run-task args)
    (catch TaskFailException task-fail-exception
      (println "The task failed:" (.getMessage task-fail-exception))
      (System/exit -1))
    (catch Throwable t
      (.printStackTrace t)
      (System/exit -1))))