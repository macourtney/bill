(ns bill.classpath
  (:require [bill.build :as build]
            [clojure.java.io :as java-io]
            [clojure.string :as string])
  (:import [java.io PushbackReader]
           [java.security MessageDigest]))

(def user-directory (java-io/file (System/getProperty "user.home")))

(def maven-directory (java-io/file user-directory ".m2"))

(def maven-repository-directory (java-io/file maven-directory "repository"))

(def bill-directory (java-io/file user-directory ".bill"))

(def bill-repository-directory (java-io/file bill-directory "repository"))

(def default-algorithm "SHA-1")
(def default-chunk-size 1024)
(def hex-digits [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f])


(defn maven-group-directory [{ :keys [group] }]
  (when group
    (reduce java-io/file maven-repository-directory (string/split (name group) #"\."))))
  
(defn maven-artifact-directory [{ :keys [artifact] :as dependency-map }]
  (when artifact
    (when-let [group-directory (maven-group-directory dependency-map)]
      (java-io/file group-directory (name artifact)))))
  
(defn maven-version-directory [{ :keys [version] :as dependency-map }]
  (when version
    (when-let [artifact-directory (maven-artifact-directory dependency-map)]
      (java-io/file artifact-directory (name version)))))

(defn maven-file-name [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (str (name artifact) "-" (name version))))
      
(defn maven-jar [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [version-directory (maven-version-directory dependency-map)]
      (java-io/file version-directory (str (maven-file-name dependency-map) ".jar")))))
      
(defn maven-jar? [dependency-map]
  (when-let [maven-jar-file (maven-jar dependency-map)]
    (.exists maven-jar-file)))

(defn maven-pom [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [version-directory (maven-version-directory dependency-map)]
      (java-io/file version-directory (str (maven-file-name dependency-map) ".pom")))))
      
(defn maven-pom? [dependency-map]
  (when-let [maven-pom-file (maven-pom dependency-map)]
    (.exists maven-pom-file)))
      
(defn bill-algorithm-directory [{ :keys [algorithm] :as dependency-map }]
  (when algorithm
    (java-io/file bill-repository-directory (name algorithm))))

(defn bill-hash-directory [{ :keys [hash] :as dependency-map }]
  (when hash
    (when-let [algorithm-directory (bill-algorithm-directory dependency-map)]
      (java-io/file algorithm-directory (name hash)))))

(defn bill-jar [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [hash-directory (bill-hash-directory dependency-map)]
      (java-io/file hash-directory (str (maven-file-name dependency-map) ".jar")))))
      
(defn bill-jar? [dependency-map]
  (when-let [bill-jar-file (bill-jar dependency-map)]
    (.exists bill-jar-file)))

(defn bill-clj [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (when-let [hash-directory (bill-hash-directory dependency-map)]
      (java-io/file hash-directory (str (maven-file-name dependency-map) ".clj")))))
      
(defn bill-clj? [dependency-map]
  (when-let [bill-clj-file (bill-clj dependency-map)]
    (.exists bill-clj-file)))
    
(defn read-bill-clj [dependency-map]
  (when (bill-clj? dependency-map)
    (read (PushbackReader. (java-io/reader (bill-clj dependency-map))))))

(defn parse-hash-vector [hash-vector]
  (cond
    (second hash-vector) { :algorithm (first hash-vector) :hash (second hash-vector) }
    (first hash-vector) { :algorithm default-algorithm :hash (first hash-vector) }))

(defn parse-dependency-symbol [dependency-symbol]
  (when dependency-symbol
    (let [artifact (name dependency-symbol)]
      { :group (or (namespace dependency-symbol) artifact)
        :artifact artifact })))
    
(defn dependency-map [dependency-vector]
  (when dependency-vector
    (if (or (vector? dependency-vector) (list? dependency-vector))
      (let [first-item (first dependency-vector)]
        (if (symbol? first-item)
          (merge
            (parse-dependency-symbol first-item)
            { :version (second dependency-vector) }
            (parse-hash-vector (drop 2 dependency-vector)))
          (parse-hash-vector dependency-vector)))
      dependency-vector)))

(defn read-byte-chunk
  ([input-stream] (read-byte-chunk input-stream default-chunk-size))
  ([input-stream buffer-size]
    (let [buffer (byte-array buffer-size)
          bytes-read (.read input-stream buffer 0 buffer-size)]
      (cond
        (= bytes-read buffer-size) buffer
        (< bytes-read 0) nil
        (< bytes-read buffer-size) (byte-array bytes-read buffer)
        :else (throw (RuntimeException. (str "Invalid number of bytes read: " bytes-read)))))))

(defn read-bytes
  ([input-stream] (read-bytes input-stream default-chunk-size))
  ([input-stream buffer-size]
    (take-while identity (repeatedly #(read-byte-chunk input-stream buffer-size)))))

(defn encode-hex-byte [byte]
  [(nth hex-digits (bit-and 15 (bit-shift-right byte 4)))
   (nth hex-digits (bit-and 15 byte))])
    
(defn encode-hex [bytes]
  (when bytes
    (string/join "" (mapcat encode-hex-byte bytes))))

(defn hash-code [file algorithm]
  (when (and file algorithm (.exists file))
    (let [message-digest (MessageDigest/getInstance algorithm)]
      (.reset message-digest)
      (with-open [file-stream (java-io/input-stream file)]
        (doseq [file-bytes (read-bytes file-stream)]
          (.update message-digest file-bytes)))
      (encode-hex (.digest message-digest)))))
      
(defn validate-hash [file algorithm file-hash]
  (= file-hash (hash-code file algorithm)))

(defn assure-hash-directory [algorithm hash]
  (let [hash-directory (bill-hash-directory { :algorithm algorithm :hash hash })]
    (when (not (.exists hash-directory))
      (.mkdirs hash-directory))
    hash-directory))

(defn move-to-repository
  ([jar-file] (move-to-repository jar-file default-algorithm))
  ([jar-file algorithm]
    (let [file-hash (hash-code jar-file algorithm)
          hash-directory (assure-hash-directory algorithm file-hash)]
      (java-io/copy jar-file (java-io/file hash-directory (.getName jar-file))))))

(defn dependencies [dependency-map]
  (when-let [bill-clj-map (read-bill-clj dependency-map)]
    (:dependencies bill-clj-map)))

(defn child-dependency-maps [parent-dependency-map]
  (filter identity (map dependency-map (dependencies parent-dependency-map))))
      
(defn group-artifact-str [dependency-map]
  (when dependency-map
    (when-let [artifact (:artifact dependency-map)]
      (str (name (or (:group dependency-map) artifact)) "/" (name artifact)))))

(defn create-classpath
  ([] (create-classpath
        { :classpath {} 
          :dependencies (map dependency-map (build/dependencies)) }))
  ([classpath-map]
    (let [classpath-dependencies (:dependencies classpath-map)]
      (if-let [dependency-map (first classpath-dependencies)]
        (let [group-artifact (group-artifact-str dependency-map)
              classpath (:classpath classpath-map)
              next-dependencies (rest classpath-dependencies)]
          (if (contains? classpath group-artifact)
            (recur (assoc classpath-map :dependencies next-dependencies))
            (recur { :classpath (assoc classpath group-artifact dependency-map)
                     :dependencies (if-let [child-dependencies (seq (child-dependency-maps dependency-map))]
                                     (if next-dependencies
                                       (concat next-dependencies child-dependencies)
                                       child-dependencies)
                                     next-dependencies) })))
        (:classpath classpath-map)))))
      
(defn resolve-dependencies
  ([] (resolve-dependencies (build/dependencies)))
  ([dependencies]
    (map bill-jar (vals (create-classpath
      { :classpath {} 
        :dependencies (map dependency-map dependencies) } )))))
      
(defn classpath [bill-dependencies]
  (resolve-dependencies (concat (build/dependencies) bill-dependencies)))