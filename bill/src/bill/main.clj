(ns bill.main
  (:require [bill.classloader :as classloader]
            [bill.init-tasks :as init-tasks] ; Loads default tasks.
            [bill.task :as task]
            [clojure.java.io :as java-io])
  (:import [org.bill TaskFailException]))

(def build-file-name "build.clj")
  
(defn run-task [args]
  (let [task (or (first args) "help")]
    (if (task/find-task task)
      (task/run-task task (rest args))
      (classloader/run-task-in-classloader task (rest args)))))

(defn load-build-file []
  (when (.exists (java-io/file build-file-name))
    (load-file build-file-name)))

(defn -main [& args]
  (binding [*read-eval* false]
    (try
      (load-build-file)
      (run-task args)
      (catch TaskFailException task-fail-exception
        (println "The task failed:" (.getMessage task-fail-exception))
        (System/exit -1))
      (catch Throwable throwable
        (.printStackTrace throwable)
        (System/exit -1)))))