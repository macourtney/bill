(ns bill.test.target
  (:use clojure.test
        bill.target))

(deftest test-targets
  (is (= (targets) {}))
  (let [targets-map { :test { :name :test :function (fn [& args] (println args)) } }]
    (targets! targets-map)
    (is (= (targets) targets-map))
    (targets! {})
    (is (= (targets) {}))))

(deftest test-add-run-target
  (let [test-target-name 'test-target
        test-target-fn (fn [& args] (println args))
        test-target-map { :parent nil :name test-target-name :function test-target-fn }]
    (is (= (targets) {}))
    (is (nil? (find-target test-target-name)))
    (add-target test-target-map)
    (is (= (find-target test-target-name) test-target-map))
    (is (= (find-target (name test-target-name)) test-target-map))
    (targets! {})
    (is (= (targets) {}))))

(deftest test-deftarget
  (let [test-target-name 'test-target
        test-args ["test" "target"]]
    (is (= (targets) {}))
    (is (nil? (find-target test-target-name)))

    (deftarget test-target [& args]
      (is (= args test-args)))

    (deftarget test-target [& args]
      (is super)
      (is (= args test-args))
      (run-target super test-args))

    (let [test-target (find-target test-target-name)]
      (is test-target)
      (is (= (:name test-target) (name test-target-name)))
      (is (:parent test-target))
      (run-target test-target-name test-args))
    (targets! {})
    (is (= (targets) {}))))