(ns bill.target)

(def target-atom (atom {}))
                  
(defn targets []
  @target-atom)
  
(defn targets! [targets-map]
  (reset! target-atom targets-map))

(defn find-target [target-name]
  (get (targets) (name target-name)))
  
(defn run-target [target-name args]
  (when-let [target (find-target target-name)]
    (apply target args)))
    
(defn add-target [target-name target-fn]
  (swap! target-atom
    #(assoc %1 (name %2) %3)
    target-name target-fn))

(defmacro deftarget [target-name & body]
  `(add-target ~target-name
    (fn [& ~'args]
      ~@body)))