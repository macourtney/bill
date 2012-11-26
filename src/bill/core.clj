(ns bill.core)

(def build-atom (atom
                  { :project-version "1.0.0-SNAPSHOT" }))
                  
(defn build []
  @build-atom)
  
(defn build! [build-map]
  (reset! build-atom build-map))

(defn update-build [build-map args]
  (build! build-map))

(defmacro defbuild [build-map & args]
  `(update-build '~build-map ~args))