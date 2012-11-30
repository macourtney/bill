(ns bill.test.classpath
  (:refer-clojure :exclude [clojure-version])
  (:use clojure.test
        bill.classpath)
  (:require [bill.build :as build]
            [bill.maven-repository :as maven-repository]
            [bill.repository :as repository]
            [clojure.java.io :as java-io]
            [clojure.string :as string]))

(def bill-hash "f462fa8d59517ff0b27deb5391d9c27e5c3eec16")
(def bill-algorithm "SHA-1")
(def bill-version "0.0.1-SNAPSHOT")
(def bill-name 'org.bill/bill-build)

(def bill-dependency [bill-name bill-version bill-algorithm bill-hash])
(def bill-dependency-map (dependency-map bill-dependency))
            
(def clojure-hash "867288bc07a6514e2e0b471c5be0bccd6c3a51f9")
(def clojure-algorithm "SHA-1")
(def clojure-version "1.4.0")
(def clojure-name 'org.clojure/clojure)
(def clojure-artifact "clojure")
(def clojure-group "org.clojure")
(def byte-char-set "UTF-8")
(def byte-array-class (Class/forName "[B"))
            
(def clojure-dependency [clojure-name clojure-version clojure-algorithm clojure-hash])
            
(def clojure-dependency-map (dependency-map clojure-dependency))
(def fail-dependency-map { :artifact :fail :version :1.0.0 :algorithm "SHA-1" :hash :fail })

(deftest test-parse-hash-vector
  (is (= { :algorithm clojure-algorithm :hash clojure-hash }
         (parse-hash-vector [clojure-algorithm clojure-hash])))
  (is (= { :algorithm clojure-algorithm :hash clojure-hash }
         (parse-hash-vector [clojure-hash])))
  (is (nil? (parse-hash-vector [])))
  (is (nil? (parse-hash-vector nil))))

(deftest test-parse-dependency-symbol
  (is (= { :group clojure-group :artifact clojure-artifact }
         (parse-dependency-symbol clojure-name)))
  (is (= { :group "clojure" :artifact clojure-artifact }
         (parse-dependency-symbol 'clojure)))
  (is (nil? (parse-dependency-symbol nil))))
  
(deftest test-dependency-map
  (is (= { :group clojure-group :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash }
         (dependency-map [clojure-name clojure-version clojure-algorithm clojure-hash])))
  (is (= { :group "clojure" :artifact clojure-artifact :version clojure-version :algorithm clojure-algorithm :hash clojure-hash }
         (dependency-map ['clojure clojure-version clojure-algorithm clojure-hash])))
  (is (= { :group "clojure" :artifact clojure-artifact :version clojure-version }
         (dependency-map ['clojure clojure-version])))
  (is (nil? (dependency-map nil))))
  
(deftest test-read-byte-chunk
  (let [byte-chunk (read-byte-chunk (java-io/input-stream (.getBytes "blah" byte-char-set)) 4)]
    (is (= (count byte-chunk) 4))
    (is (instance? byte-array-class byte-chunk)))
  (let [byte-chunk (read-byte-chunk (java-io/input-stream (.getBytes "blahblah" byte-char-set)) 4)]
    (is (= (count byte-chunk) 4))
    (is (instance? byte-array-class byte-chunk)))
  (let [byte-chunk (read-byte-chunk (java-io/input-stream (.getBytes "blah" byte-char-set)) 8)]
    (is (= (count byte-chunk) 4))
    (is (instance? byte-array-class byte-chunk)))
  (let [byte-chunk (read-byte-chunk (java-io/input-stream (.getBytes "" byte-char-set)) 4)]
    (is (nil? byte-chunk))))

(deftest test-read-bytes
  (let [byte-seq (doall (read-bytes (java-io/input-stream (.getBytes "blahblah" byte-char-set)) 4))]
    (is byte-seq)
    (is (= (count byte-seq) 2))
    (doseq [byte-array byte-seq]
      (is (= (count byte-array) 4)))))

(deftest test-encode-hex-byte
  (let [hex-code-map {   0 [\0 \0]   1 [\0 \1]   2 [\0 \2]   3 [\0 \3]   4 [\0 \4]   5 [\0 \5]   6 [\0 \6]   7 [\0 \7]   8 [\0 \8]   9 [\0 \9]  10 [\0 \a]  11 [\0 \b]  12 [\0 \c]  13 [\0 \d]  14 [\0 \e]  15 [\0 \f]
                        16 [\1 \0]  17 [\1 \1]  18 [\1 \2]  19 [\1 \3]  20 [\1 \4]  21 [\1 \5]  22 [\1 \6]  23 [\1 \7]  24 [\1 \8]  25 [\1 \9]  26 [\1 \a]  27 [\1 \b]  28 [\1 \c]  29 [\1 \d]  30 [\1 \e]  31 [\1 \f]
                        32 [\2 \0]  33 [\2 \1]  34 [\2 \2]  35 [\2 \3]  36 [\2 \4]  37 [\2 \5]  38 [\2 \6]  39 [\2 \7]  40 [\2 \8]  41 [\2 \9]  42 [\2 \a]  43 [\2 \b]  44 [\2 \c]  45 [\2 \d]  46 [\2 \e]  47 [\2 \f]
                        48 [\3 \0]  49 [\3 \1]  50 [\3 \2]  51 [\3 \3]  52 [\3 \4]  53 [\3 \5]  54 [\3 \6]  55 [\3 \7]  56 [\3 \8]  57 [\3 \9]  58 [\3 \a]  59 [\3 \b]  60 [\3 \c]  61 [\3 \d]  62 [\3 \e]  63 [\3 \f]
                        64 [\4 \0]  65 [\4 \1]  66 [\4 \2]  67 [\4 \3]  68 [\4 \4]  69 [\4 \5]  70 [\4 \6]  71 [\4 \7]  72 [\4 \8]  73 [\4 \9]  74 [\4 \a]  75 [\4 \b]  76 [\4 \c]  77 [\4 \d]  78 [\4 \e]  79 [\4 \f]
                        80 [\5 \0]  81 [\5 \1]  82 [\5 \2]  83 [\5 \3]  84 [\5 \4]  85 [\5 \5]  86 [\5 \6]  87 [\5 \7]  88 [\5 \8]  89 [\5 \9]  90 [\5 \a]  91 [\5 \b]  92 [\5 \c]  93 [\5 \d]  94 [\5 \e]  95 [\5 \f]
                        96 [\6 \0]  97 [\6 \1]  98 [\6 \2]  99 [\6 \3] 100 [\6 \4] 101 [\6 \5] 102 [\6 \6] 103 [\6 \7] 104 [\6 \8] 105 [\6 \9] 106 [\6 \a] 107 [\6 \b] 108 [\6 \c] 109 [\6 \d] 110 [\6 \e] 111 [\6 \f]
                       112 [\7 \0] 113 [\7 \1] 114 [\7 \2] 115 [\7 \3] 116 [\7 \4] 117 [\7 \5] 118 [\7 \6] 119 [\7 \7] 120 [\7 \8] 121 [\7 \9] 122 [\7 \a] 123 [\7 \b] 124 [\7 \c] 125 [\7 \d] 126 [\7 \e] 127 [\7 \f]
                       128 [\8 \0] 129 [\8 \1] 130 [\8 \2] 131 [\8 \3] 132 [\8 \4] 133 [\8 \5] 134 [\8 \6] 135 [\8 \7] 136 [\8 \8] 137 [\8 \9] 138 [\8 \a] 139 [\8 \b] 140 [\8 \c] 141 [\8 \d] 142 [\8 \e] 143 [\8 \f]
                       144 [\9 \0] 145 [\9 \1] 146 [\9 \2] 147 [\9 \3] 148 [\9 \4] 149 [\9 \5] 150 [\9 \6] 151 [\9 \7] 152 [\9 \8] 153 [\9 \9] 154 [\9 \a] 155 [\9 \b] 156 [\9 \c] 157 [\9 \d] 158 [\9 \e] 159 [\9 \f]
                       160 [\a \0] 161 [\a \1] 162 [\a \2] 163 [\a \3] 164 [\a \4] 165 [\a \5] 166 [\a \6] 167 [\a \7] 168 [\a \8] 169 [\a \9] 170 [\a \a] 171 [\a \b] 172 [\a \c] 173 [\a \d] 174 [\a \e] 175 [\a \f]
                       176 [\b \0] 177 [\b \1] 178 [\b \2] 179 [\b \3] 180 [\b \4] 181 [\b \5] 182 [\b \6] 183 [\b \7] 184 [\b \8] 185 [\b \9] 186 [\b \a] 187 [\b \b] 188 [\b \c] 189 [\b \d] 190 [\b \e] 191 [\b \f]
                       192 [\c \0] 193 [\c \1] 194 [\c \2] 195 [\c \3] 196 [\c \4] 197 [\c \5] 198 [\c \6] 199 [\c \7] 200 [\c \8] 201 [\c \9] 202 [\c \a] 203 [\c \b] 204 [\c \c] 205 [\c \d] 206 [\c \e] 207 [\c \f]
                       208 [\d \0] 209 [\d \1] 210 [\d \2] 211 [\d \3] 212 [\d \4] 213 [\d \5] 214 [\d \6] 215 [\d \7] 216 [\d \8] 217 [\d \9] 218 [\d \a] 219 [\d \b] 220 [\d \c] 221 [\d \d] 222 [\d \e] 223 [\d \f]
                       224 [\e \0] 225 [\e \1] 226 [\e \2] 227 [\e \3] 228 [\e \4] 229 [\e \5] 230 [\e \6] 231 [\e \7] 232 [\e \8] 233 [\e \9] 234 [\e \a] 235 [\e \b] 236 [\e \c] 237 [\e \d] 238 [\e \e] 239 [\e \f]
                       240 [\f \0] 241 [\f \1] 242 [\f \2] 243 [\f \3] 244 [\f \4] 245 [\f \5] 246 [\f \6] 247 [\f \7] 248 [\f \8] 249 [\f \9] 250 [\f \a] 251 [\f \b] 252 [\f \c] 253 [\f \d] 254 [\f \e] 255 [\f \f] }]
    (doseq [hex-code-pair hex-code-map]
      (is (= (encode-hex-byte (first hex-code-pair)) (second hex-code-pair))))))

(deftest test-encode-hex
  (is (= (encode-hex [0 17 34 51]) "00112233"))
  (is (= (encode-hex []) ""))
  (is (nil? (encode-hex nil))))

(deftest test-hash-code
  (let [clojure-jar (maven-repository/maven-jar (dependency-map [clojure-name clojure-version]))]
    (is clojure-jar)
    (is (.exists clojure-jar))
    (is (= (hash-code clojure-jar clojure-algorithm) clojure-hash)))
  (let [bill-jar (java-io/file (maven-repository/maven-jar bill-dependency-map))] ;(bill-jar bill-dependency-map)
    (is bill-jar)
    (is (.exists bill-jar))
    (is (= (hash-code bill-jar bill-algorithm) bill-hash))))
    
(deftest test-validate-hash
  (is (validate-hash (maven-repository/maven-jar (dependency-map [clojure-name clojure-version])) clojure-algorithm clojure-hash))
  (is (validate-hash (repository/bill-jar bill-dependency-map) bill-algorithm bill-hash))
  (is (not (validate-hash (maven-repository/maven-jar (dependency-map [clojure-name clojure-version])) clojure-algorithm "fail"))))
    
(deftest test-move-to-repository
  (let [clojure-jar (maven-repository/maven-jar clojure-dependency-map)
        bill-clojure-jar (repository/bill-jar clojure-dependency-map)]
    (when (.exists bill-clojure-jar)
      (.delete bill-clojure-jar))
    (move-to-repository clojure-jar clojure-algorithm)
    (is (.exists bill-clojure-jar))))

(deftest test-dependencies
  (is (= (dependencies clojure-dependency-map) []))
  (is (nil? (dependencies fail-dependency-map))))

(deftest test-serialize-clj
  (is (= (serialize-clj 1) "1"))
  (is (= (serialize-clj "blah") "\"blah\""))
  (is (= (serialize-clj { :foo "bar" }) "#=(clojure.lang.PersistentArrayMap/create {:foo \"bar\"})")))

(deftest test-group-artifact-str
  (is (= (group-artifact-str clojure-dependency-map) (str clojure-name)))
  (is (= (group-artifact-str { :artifact clojure-artifact }) (str clojure-artifact "/" clojure-artifact))))

(deftest test-create-classpath
  (is (= (create-classpath { :classpath {} 
                             :dependencies [clojure-dependency-map] })
         { (str clojure-name) clojure-dependency-map })))
  
(deftest test-classpath
  (let [old-build (build/build)]
    (build/build!
      { :dependencies [clojure-dependency] })
    (is (= (classpath [bill-dependency]) [(repository/bill-jar bill-dependency-map) (repository/bill-jar clojure-dependency-map)]))
    (build/build!
      { :dependencies [bill-dependency] })
    (is (= (classpath []) [(repository/bill-jar clojure-dependency-map) (repository/bill-jar bill-dependency-map)]))
    (build/build! old-build)))