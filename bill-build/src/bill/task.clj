(ns bill.task
  (:import [org.bill TaskFailException])
  (:require [bill.build :as build]))

(def task-bill-atom (atom {}))
(def task-build-atom (atom {}))

(defn tasks []
  (if build/build-environment?
    @task-build-atom
    @task-bill-atom))

(defn tasks! [tasks-map]
  (if build/build-environment?
    (reset! task-build-atom tasks-map)
    (reset! task-bill-atom tasks-map)))

(defn task-name [task]
  (:name task))

(defn task-function [task]
  (:function task))

(defn task-description [task]
  (:description task))

(defn find-task [task-name]
  (get (tasks) (name task-name)))

(defn run-task [task args]
  (when-let [task (if (map? task) task (find-task task))]
    (let [parent-map (:parent task)]
      (apply (task-function task) parent-map args))))

(defn add-task [task-map]
  (let [task-name (:name task-map)
        parent-task (find-task task-name)
        task-map (assoc task-map :parent parent-task)]
    (if build/build-environment?
      (swap! task-build-atom #(assoc %1 (name %2) %3) task-name task-map)
      (swap! task-bill-atom #(assoc %1 (name %2) %3) task-name task-map))))

(defn remove-task [task-name]
  (if build/build-environment?
    (swap! task-build-atom #(dissoc %1 (name %2)) task-name)
    (swap! task-bill-atom #(dissoc %1 (name %2)) task-name)))

(defmacro deftask [task-name arg-vec & body]
  (let [task-name-str (name task-name)
        task-name-symbol (symbol task-name-str)
        description (when (string? arg-vec) arg-vec)
        arg-vec (vec (concat ['super] (if description (first body) arg-vec)))
        body (if description (rest body) body)]
    `(add-task
      { :name ~task-name-str
        :description ~description
        :function (fn ~task-name-symbol ~arg-vec ~@body) })))

(defn fail [message]
  (throw (TaskFailException. message)))