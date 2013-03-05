(ns bill.test.task
  (:use clojure.test
        bill.task)
  (:import [org.bill TaskFailException]))

(deftest test-tasks
  (let [old-tasks (tasks)
        tasks-map { :test { :name :test :function (fn [& args] (println args)) } }]
    (tasks! {})
    (is (= (tasks) {}))
    (tasks! tasks-map)
    (is (= (tasks) tasks-map))
    (tasks! old-tasks)
    (is (= (tasks) old-tasks))))

(deftest test-add-run-task
  (let [old-tasks (tasks)
        test-task-name 'test-task
        test-task-fn (fn [& args] (println args))
        test-task-map { :parent nil :name test-task-name :function test-task-fn }]
    (tasks! {})
    (is (= (tasks) {}))
    (is (nil? (find-task test-task-name)))
    (add-task test-task-map)
    (is (= (find-task test-task-name) test-task-map))
    (is (= (find-task (name test-task-name)) test-task-map))
    (remove-task test-task-name)
    (is (nil? (find-task test-task-name)))
    (tasks! old-tasks)
    (is (= (tasks) old-tasks))))

(deftest test-deftask
  (let [old-tasks (tasks)
        test-task-name 'test-task
        test-args ["test" "task"]]
    (tasks! {})
    (is (= (tasks) {}))
    (is (nil? (find-task test-task-name)))

    (deftask test-task [& args]
      (is (= args test-args)))

    (deftask test-task [& args]
      (is super)
      (is (= args test-args))
      (run-task super test-args))

    (let [test-task (find-task test-task-name)]
      (is test-task)
      (is (= (:name test-task) (name test-task-name)))
      (is (:parent test-task))
      (run-task test-task-name test-args))
    (tasks! old-tasks)
    (is (= (tasks) old-tasks))))

(deftest test-fail
  (let [message "Totally valid failure."]
    (try
      (fail message)
      (is false "An TaskFailException was expected.")
      (catch TaskFailException task-fail-exception
        (is task-fail-exception)
        (is (= (.getMessage task-fail-exception) message))
        (is (nil? (.getCause task-fail-exception)))))))