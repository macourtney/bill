(ns build
  (:use bill.core))

(defbuild
  { :project [org.bill/bill-build "0.0.1-SNAPSHOT"]
    :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."

    :dependencies [[org.clojure/clojure "1.4.0" "SHA-1" "3752cfd2c96be72fb96fcf79a2a546969dfda480"]
                   [org.clojure/tools.namespace "0.2.2" "SHA-1" "f3de89d584f15c17408bff1a04522d1a792a0bd2"]] })