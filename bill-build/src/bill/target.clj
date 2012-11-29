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
    (apply (:function target) args)))
    
(defn add-target [target-map]
  (swap! target-atom
    #(assoc %1 (name %2) %3)
    (:name target-map) target-map))

(defmacro deftarget [target-name arg-vec & body]
  (let [target-name-str (name target-name)
        target-name-symbol (symbol target-name-str)
        description (when (string? arg-vec) arg-vec)
        arg-vec (if description (first body) arg-vec)
        body (if description (rest body) body)]
    `(add-target
      { :name ~target-name-str
        :description ~description
        :function (fn ~target-name-symbol ~arg-vec ~@body) })))