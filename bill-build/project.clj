(defproject org.bill/bill-build "0.0.1-SNAPSHOT"
  :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.namespace "0.2.2"]]

  :aot [bill.task-fail-exception])