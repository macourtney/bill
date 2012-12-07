(ns bill.util
  (:require [clojure.java.io :as java-io]
            [clojure.string :as string])
  (:import [java.io StringWriter]
           [java.security MessageDigest]))

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

(defn write-form [file form]
  (with-open [bill-clj-writer (java-io/writer file)]
    (binding [*out* bill-clj-writer]
      (println (serialize-clj form)))))

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