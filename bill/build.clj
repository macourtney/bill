(ns build
  (:use bill.core))

(defbuild
  { :project [org.bill/bill "0.0.1-SNAPSHOT"]
    :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."

    :dependencies [[org.bill/bill-build "0.0.1-SNAPSHOT" "SHA-1" "c00cb8a17eae801a7125e59caab4ce71432f086a"]
                   [classlojure "0.6.6" "SHA-1" "0a683a68fd4a6c3f494efc557889332028f3f592"]
                   [org.clojure/clojure "1.4.0" "SHA-1" "c5aed5373965d1979dd249dd9f38d7bb5b2ee1c2"]
                   [org.clojure/tools.cli "0.2.2" "SHA-1" "41324d803e5b14fc8ccc01463e28d91f86c706d9"]] })