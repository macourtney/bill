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
        test-target-map { :name test-target-name :function test-target-fn }]
    (is (= (targets) {}))
    (is (nil? (find-target test-target-name)))
    (add-target test-target-map)
    (is (= (find-target test-target-name) test-target-map))
    (is (= (find-target (name test-target-name)) test-target-map))
    (targets! {})
    (is (= (targets) {}))))

(deftest test-deftarget
  (let [test-target-name 'test-target]
    (is (= (targets) {}))
    (is (nil? (find-target test-target-name)))
    (deftarget test-target [& args]
      (println "args:" args))
    (is (find-target test-target-name))
    (is (= (:name (find-target test-target-name)) (name test-target-name)))
    (targets! {})
    (is (= (targets) {}))))