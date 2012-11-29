(ns bill.build)

(def build-atom (atom
                  {}))
                  
(defn build []
  @build-atom)
  
(defn build! [build-map]
  (reset! build-atom build-map))

(defn project []
  (:project (build)))

(defn dependencies []
  (:dependencies (build)))  