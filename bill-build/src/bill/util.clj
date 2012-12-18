(ns bill.util
  (:require [clojure.java.io :as java-io]
            [clojure.string :as string])
  (:import [java.io ByteArrayOutputStream File OutputStreamWriter StringWriter]
           [java.security MessageDigest]))

(def default-algorithm "SHA-1")
(def user-directory (java-io/file (System/getProperty "user.home")))
(def default-chunk-size 1024)
(def hex-digits [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f])

(defn serialize-clj [form]
  (when form
    (let [string-writer (new StringWriter)]
      (binding [*print-dup* true]
        (binding [*out* string-writer]
          (print form)))
      (.close string-writer)
      (.toString string-writer))))

(defn write-form [writer form]
  (with-open [bill-clj-writer (java-io/writer writer)]
    (binding [*out* bill-clj-writer]
      (println (serialize-clj form)))))

(defn form-bytes [form]
  (with-open [byte-array-output-stream (new ByteArrayOutputStream)
              stream-writer (OutputStreamWriter. byte-array-output-stream)]
    (write-form stream-writer form)
    (.toByteArray byte-array-output-stream)))

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

(defn validate-input-stream [input-stream]
  (when input-stream
    (if (instance? File input-stream)
      (when (.exists input-stream)
        input-stream)
      input-stream)))

(defn hash-code [input-stream algorithm]
  (when (and (validate-input-stream input-stream) algorithm)
    (let [message-digest (MessageDigest/getInstance algorithm)]
      (.reset message-digest)
      (with-open [file-stream (java-io/input-stream input-stream)]
        (doseq [file-bytes (read-bytes file-stream)]
          (.update message-digest file-bytes)))
      (encode-hex (.digest message-digest)))))
      
(defn validate-hash [input-stream algorithm file-hash]
  (= file-hash (hash-code input-stream algorithm)))

(defn form-hash-code [form algorithm]
  (hash-code (form-bytes form) algorithm))

(defn dependency-vector [dependency-map]
  (let [group (:group dependency-map)
        artifact (:artifact dependency-map)
        group-artifact-str (if (and group (not (= group artifact))) (str group "/" artifact) artifact)]
    [(when group-artifact-str (symbol group-artifact-str))
     (:version dependency-map) (:algorithm dependency-map) (:hash dependency-map)]))

(defn dependency-vector-str [dependency-map]
  (serialize-clj (dependency-vector dependency-map)))

(defn file-name [{ :keys [artifact version] :as dependency-map }]
  (when (and artifact version)
    (str (name artifact) "-" (name version))))