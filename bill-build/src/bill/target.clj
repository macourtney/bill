(ns bill.target)

(def target-atom (atom {}))
                  
(defn targets []
  @target-atom)

(defn targets! [targets-map]
  (reset! target-atom targets-map))

(defn find-target [target-name]
  (get (targets) (name target-name)))

(defn run-target [target args]
  (when-let [target (if (map? target) target (find-target target))]
    (let [parent-map (:parent target)]
      (apply (:function target) parent-map args))))

(defn add-target [target-map]
  (let [target-name (:name target-map)
        parent-target (find-target target-name)
        target-map (assoc target-map :parent parent-target)]
    (swap! target-atom #(assoc %1 (name %2) %3) target-name target-map)))

(defmacro deftarget [target-name arg-vec & body]
  (let [target-name-str (name target-name)
        target-name-symbol (symbol target-name-str)
        description (when (string? arg-vec) arg-vec)
        arg-vec (vec (concat ['super] (if description (first body) arg-vec)))
        body (if description (rest body) body)]
    `(add-target
      { :name ~target-name-str
        :description ~description
        :function (fn ~target-name-symbol ~arg-vec ~@body) })))