(ns build
  (:use bill.core))

(defbuild
  { :project [org.bill/bill "0.0.1-SNAPSHOT"]
    :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."

    :dependencies [[org.bill/bill-build "0.0.1-SNAPSHOT" "SHA-1" "79359f4b714b0eecb4ea2cc38a8b8b508bb81b91"]
                   [classlojure "0.6.6"]
                   [commons-codec "20041127.091804"]
                   [org.clojure/clojure "1.4.0" "SHA-1" "f25fcaa3afe2d96b7cf825dbf9ff6de4a8b514ec"]
                   [org.clojure/tools.cli "0.2.2"]] })