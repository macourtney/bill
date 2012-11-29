(ns build
  (:use bill.core))

(defbuild
  { :project [org.bill/bill "0.0.1-SNAPSHOT"]
    :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."

    :dependencies [[org.bill/bill-build "0.0.1-SNAPSHOT"]
                   [classlojure "0.6.6"]
                   [commons-codec "20041127.091804"]
                   [org.clojure/clojure "1.4.0" "SHA-1" "867288bc07a6514e2e0b471c5be0bccd6c3a51f9"]] })