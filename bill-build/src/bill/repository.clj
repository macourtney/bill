(ns bill.repository
  (:require [bill.build :as build]
            [bill.util :as util]
            [clojure.java.io :as java-io]
            [clojure.string :as string])
  (:import [java.io PushbackReader]
           [java.security MessageDigest]))

(def bill-directory (java-io/file util/user-directory ".bill"))

(def bill-repository-directory (java-io/file bill-directory "repository"))

(def bill-temp-directory (java-io/file bill-directory "temp"))

(defn file-name [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (str (name artifact) "-" (name version))))

(defn bill-algorithm-directory [{ :keys [algorithm] :as dependency-map }]
  (when algorithm
    (java-io/file bill-repository-directory (name algorithm))))

(defn bill-hash-directory [{ :keys [hash] :as dependency-map }]
  (when hash
    (when-let [algorithm-directory (bill-algorithm-directory dependency-map)]
      (java-io/file algorithm-directory (name hash)))))

(defn jar-file-name [dependency-map]
  (str (file-name dependency-map) ".jar"))
      
(defn bill-jar [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [hash-directory (bill-hash-directory dependency-map)]
      (java-io/file hash-directory (jar-file-name dependency-map)))))
      
(defn bill-jar? [dependency-map]
  (when-let [bill-jar-file (bill-jar dependency-map)]
    (.exists bill-jar-file)))

(defn bill-clj-file-name [dependency-map]
  (str (file-name dependency-map) ".clj"))

(defn bill-clj [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [hash-directory (bill-hash-directory dependency-map)]
      (java-io/file hash-directory (bill-clj-file-name dependency-map)))))

(defn temp-bill-clj [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (java-io/file bill-temp-directory (bill-clj-file-name dependency-map))))
      
(defn bill-clj? [dependency-map]
  (when-let [bill-clj-file (bill-clj dependency-map)]
    (.exists bill-clj-file)))

(defn target-bill-clj [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [target-path (build/target-path)]
      (java-io/file target-path (bill-clj-file-name dependency-map)))))

(defn target-bill-clj? [dependency-map]
  (when-let [bill-clj-file (target-bill-clj dependency-map)]
    (.exists bill-clj-file)))

(defn target-bill-clj-hash [dependency-map]
  (when-let [bill-clj-file (target-bill-clj dependency-map)]
    (let [algorithm util/default-algorithm]
      { :hash (util/hash-code bill-clj-file algorithm)
        :algorithm algorithm })))

(defn target-bill-clj-dependency-map [dependency-map]
  (when-let [hash-map (target-bill-clj-hash dependency-map)]
    (merge hash-map (select-keys dependency-map [:artifact :group :version]))))

(defn target-bill-clj-dependency-vector [dependency-map]
  (let [dependency-map (target-bill-clj-dependency-map dependency-map)]
    (util/dependency-vector dependency-map)))

(defn read-bill-clj-file [bill-clj-file]
  (when (and bill-clj-file (.exists bill-clj-file))
    (read (PushbackReader. (java-io/reader bill-clj-file)))))

(defn read-bill-clj [dependency-map]
  (when (bill-clj? dependency-map)
    (read-bill-clj-file (bill-clj dependency-map))))

(defn write-bill-clj [bill-clj-file bill-clj-map]
  (.mkdirs (.getParentFile bill-clj-file))
  (util/write-form bill-clj-file bill-clj-map))