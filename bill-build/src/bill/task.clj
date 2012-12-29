(ns bill.task)

(def task-atom (atom {}))
                  
(defn tasks []
  @task-atom)

(defn tasks! [tasks-map]
  (reset! task-atom tasks-map))

(defn find-task [task-name]
  (get (tasks) (name task-name)))

(defn run-task [task args]
  (when-let [task (if (map? task) task (find-task task))]
    (let [parent-map (:parent task)]
      (apply (:function task) parent-map args))))

(defn add-task [task-map]
  (let [task-name (:name task-map)
        parent-task (find-task task-name)
        task-map (assoc task-map :parent parent-task)]
    (swap! task-atom #(assoc %1 (name %2) %3) task-name task-map)))

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