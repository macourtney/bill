(defproject org.bill/bill "0.0.1-SNAPSHOT"
  :description "Bill the builder is a build tool for clojure. Unlike leiningen, bill uses the hash code of a jar file to determine the authenticity of the jar."
  :dependencies [[classlojure "0.6.6"]
                 [commons-codec "20041127.091804"]
                 [org.bill/bill-build "0.0.1-SNAPSHOT"]
                 [org.clojure/clojure "1.4.0"]])