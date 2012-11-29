(ns bill.test.target
  (:use clojure.test
        bill.target))

(deftest test-targets
  (is (= (targets) {}))
  (let [targets-map { :test (fn [& args] (println args)) }]
    (targets! targets-map)
    (is (= (targets) targets-map))
    (targets! {})
    (is (= (targets) {}))))

(deftest test-add-run-target
  (let [test-target-name 'test-target-name
        test-target-fn (fn [& args] (println args))]
    (is (= (targets) {}))
    (is (nil? (find-target test-target-name)))
    (add-target test-target-name test-target-fn)
    (is (= (find-target test-target-name) test-target-fn))
    (is (= (find-target (name test-target-name)) test-target-fn))
    (targets! {})
    (is (= (targets) {}))))