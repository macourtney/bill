(ns bill.targets.deps
  (:use bill.target)
  (:require [bill.build :as build]
            [bill.targets.install-maven :as install-maven]
            [bill.util :as util]))

(defn install-dependency [installed? dependency]
  (and (install-maven/install-dependency (util/dependency-map dependency)) installed?))

(defn install-dependencies [dependencies]
  (reduce install-dependency false dependencies))

(deftarget deps [& args]
  (if-let [dependencies (build/dependencies)]
    (when-not (install-dependencies dependencies)
      (println "All dependencies already installed."))
    (println "No dependencies found.")))