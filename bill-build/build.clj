(ns build
  (:use bill.core))

(defbuild
  { :project [org.bill/bill-build "0.0.1-SNAPSHOT"]
    :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."

    :dependencies [[org.clojure/clojure "1.4.0" "SHA-1" "c5aed5373965d1979dd249dd9f38d7bb5b2ee1c2"]] })