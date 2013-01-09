(ns bill.tasks.test
  (:use bill.task))

(deftask test [& args]
  (println "Running tests"))