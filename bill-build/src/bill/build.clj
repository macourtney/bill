(ns bill.build
  (:require [clojure.java.io :as java-io]))

(def build-defaults
  {
    ; The hash algorithm to use.
    :hash-algorithm "SHA-1"

    ;; If you'd rather use a different directory structure, you can set these.
    ;; Paths that contain "inputs" are vectors, "outputs" are strings.
    :source-paths ["src"]
    :java-source-paths ["java"] ; Java source is stored separately.
    :test-paths ["test"]
    :resource-paths ["resources"] ; non-code files included in classpath/jar
    :compile-path "target/classes" ; for .class files
    :target-path "target/" ; where to place the project's jar file
    
    :source-extensions ["clj"]
    :compiled-extensions ["class"]
    
    :manifest {} ; The attributes of the manifest file.
  })

(def build-atom (atom build-defaults))

(defn build []
  @build-atom)

(defn build! [build-map]
  (reset! build-atom build-map))

(defn update-build! [build-map]
  (build! (merge (build) build-map)))

(defn build-file []
  (java-io/file "build.clj"))

(defn project []
  (:project (build)))

(defn dependencies []
  (:dependencies (build)))

(defn target-path []
  (:target-path (build)))

(defn source-paths []
  (:source-paths (build)))

(defn source-path-files []
  (map java-io/file (source-paths)))

(defn compile-path []
  (:compile-path (build)))

(defn compile-path-file []
  (java-io/file (compile-path)))

(defn test-paths []
  (:test-paths (build)))

(defn test-path-files []
  (map java-io/file (test-paths)))

(defn resource-paths []
  (:resource-paths (build)))

(defn resource-path-files []
  (map java-io/file (resource-paths)))

(defn artifact-id []
  (when-let [group-artifact (first (project))]
    (name group-artifact)))

(defn group-id []
  (when-let [group-artifact (first (project))]
    (or (namespace group-artifact) (artifact-id))))

(defn version []
  (second (project)))
  
(defn hash-algorithm []
  (:hash-algorithm (build)))

(defn jar-base-name []
  (when-let [artifact-id (artifact-id)]
    (when-let [version (version)]
      (str artifact-id "-" version))))

(defn jar-name []
  (or (:jar-name (build))
    (when-let [jar-base-name (jar-base-name)]
      (str jar-base-name ".jar"))))

(defn target-jar-file []
  (when-let [target (target-path)]
    (when-let [jar (jar-name)]
      (java-io/file target jar))))

(defn uberjar-name []
  (or (:uberjar-name (build))
    (when-let [jar-base-name (jar-base-name)]
      (str jar-base-name "-standalone.jar"))))

(defn target-uberjar-file []
  (when-let [target (target-path)]
    (when-let [jar (uberjar-name)]
      (java-io/file target jar))))

(defn main []
  (:main (build)))

(defn main-class-name []
  (when-let [main (main)]
    (.replaceAll (name main) "-" "_")))

(defn manifest []
  (let [manifest (:manifest (build))]
    (if-let [main (main-class-name)]
      (assoc manifest "Main-Class" main)
      manifest)))

(defn source-extensions []
  (:source-extensions (build)))

(defn compiled-extensions []
  (:compiled-extensions (build)))

(defn bill-version []
  (:bill-version (build)))