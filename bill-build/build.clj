(ns build
  (:use bill.core))

(defbuild
  { :project [org.bill/bill-build "0.0.1-SNAPSHOT"]
    :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."

    :dependencies [[org.clojure/clojure "1.4.0" "SHA-1" "c5aed5373965d1979dd249dd9f38d7bb5b2ee1c2"]
                   [org.clojure/tools.namespace "0.2.2" "SHA-1" "4feabeada43d50bf6e90acd3a7fc7148e288dbc1"]] })