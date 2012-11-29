(ns build
  (:use bill.core))

(defbuild { :project [org.bill/bill "0.0.1-SNAPSHOT"]
            :dependencies [[org.clojure/clojure "1.4.0"]] })