(ns bill.test.task
  (:use clojure.test
        bill.task))

(deftest test-tasks
  (is (= (tasks) {}))
  (let [tasks-map { :test { :name :test :function (fn [& args] (println args)) } }]
    (tasks! tasks-map)
    (is (= (tasks) tasks-map))
    (tasks! {})
    (is (= (tasks) {}))))

(deftest test-add-run-task
  (let [test-task-name 'test-task
        test-task-fn (fn [& args] (println args))
        test-task-map { :parent nil :name test-task-name :function test-task-fn }]
    (is (= (tasks) {}))
    (is (nil? (find-task test-task-name)))
    (add-task test-task-map)
    (is (= (find-task test-task-name) test-task-map))
    (is (= (find-task (name test-task-name)) test-task-map))
    (tasks! {})
    (is (= (tasks) {}))))

(deftest test-deftask
  (let [test-task-name 'test-task
        test-args ["test" "task"]]
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
    (tasks! {})
    (is (= (tasks) {}))))