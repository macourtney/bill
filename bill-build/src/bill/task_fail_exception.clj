(ns bill.task-fail-exception
  (:gen-class
    :name org.bill.TaskFailException
    :extends java.lang.RuntimeException
    :state state
    :init init
    :constructors {[String] [String]}))

(defn -init [message]
  [[message] nil])