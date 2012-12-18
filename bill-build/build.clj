(ns build
  (:use bill.core))

(defbuild
  { :project [org.bill/bill-build "0.0.1-SNAPSHOT"]
    :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."
  
    :dependencies [[org.clojure/clojure "1.4.0" "SHA-1" "f25fcaa3afe2d96b7cf825dbf9ff6de4a8b514ec"]] })