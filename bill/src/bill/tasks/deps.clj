(ns bill.tasks.deps
  (:use bill.task)
  (:require [bill.build :as build]
            [bill.tasks.install-maven :as install-maven]
            [bill.util :as util]))

(defn install-dependency [installed? dependency]
  (or (install-maven/install-dependency (util/dependency-map dependency)) installed?))

(defn install-dependencies [dependencies]
  (reduce install-dependency false dependencies))

(deftask deps
  "Ensure all dependencies are loaded into the repository."
  [& args]
  (if-let [dependencies (build/dependencies)]
    (when-not (install-dependencies dependencies)
      (println "All dependencies already installed."))
    (println "No dependencies found.")))