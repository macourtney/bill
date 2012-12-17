(ns bill.xml
  (:require [clojure.xml :as xml]))

(defn parse [s]
  (when s
    (xml/parse s)))
  
(defn element-name [element]
  (:tag element))
  
(defn attributes [element]
  (:attrs element))

(defn content [element]
  (:content element))

(defn attribute-value [element attribute]
  (get (attributes element) attribute))

(defn element? [content]
  (instance? clojure.lang.PersistentStructMap content))
  
(defn child-elements [element]
  (filter element? (content element)))

(defn text [element]
  (filter string? (content element)))

(defn to-element-name [element-name]
  (when element-name
    (if (keyword? element-name)
      element-name
      (keyword (name element-name)))))
  
(defn is-element? [element name]
  (when element
    (= (element-name element) (to-element-name name))))
  
(defn find-children [element child-name]
  (filter #(is-element? % child-name) (child-elements element)))

(defn find-child [element child-name]
  (first (find-children element child-name)))