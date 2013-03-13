(ns build
  (:use bill.core))

(defbuild
  { :project [org.bill/bill "0.0.1-SNAPSHOT"]
    :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."

    :dependencies [[org.bill/bill-build "0.0.1-SNAPSHOT" "SHA-1" "1f330c2845d1b9a69b26cf84141ff9d26e06e763"]
                   [classlojure "0.6.6" "SHA-1" "4dc33e0f7aebdfd96036114aee4efb0e5e9e6994"]
                   [org.clojure/clojure "1.4.0" "SHA-1" "3752cfd2c96be72fb96fcf79a2a546969dfda480"]
                   [org.clojure/tools.cli "0.2.2" "SHA-1" "ff8645109243c6d9acd2a50b93251a2e3e0affcd"]] })