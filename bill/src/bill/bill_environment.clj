(ns bill.bill-environment)

(defn eval-in [forms]
  (doseq [form forms]
    (eval form)))