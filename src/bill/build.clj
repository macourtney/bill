(ns bill.build)

(def build-atom (atom
                  { :project-version "1.0.0-SNAPSHOT" }))
                  
(defn build []
  @build-atom)
  
(defn build! [build-map]
  (reset! build-atom build-map))