(ns bill.main
  (:require [bill.core :as core]
            [bill.target :as target]))

(defn run-target [args]
  (let [target (or (first args) "help")]
    (if (target/find-target target)
      (target/run-target target (rest args))
      (core/run-target-in-classloader target (rest args)))))
            
(defn -main [& args]
  (load-file "build.clj")
  (run-target args))