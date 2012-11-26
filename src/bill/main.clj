(ns bill.main
  (:require [classlojure.core :as classlojure]
            [clojure.java.io :as java-io]))

(defn -main [& args]
  (load-file "build.clj"))