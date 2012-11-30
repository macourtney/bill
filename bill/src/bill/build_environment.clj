(ns bill.build-environment)

(defn eval-in-build [forms]
  (doseq [form forms]
    (eval form)))