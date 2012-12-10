(ns bill.test.xml
  (:use clojure.test
        bill.xml)
  (:require [clojure.xml :as xml])
  (:import [java.io ByteArrayInputStream]))

(def test-xml-string "<test foo=\"bar\"><child>child text</child>text</test>")
(def test-xml-stream (ByteArrayInputStream. (.getBytes test-xml-string)))

(def foo-attributes { :foo "bar" })
(def child-element (struct xml/element :child nil ["child text"]))
(def test-xml-content [child-element "text"])
(def test-xml-element (struct xml/element :test foo-attributes test-xml-content))

(deftest test-parse
  (is (= (parse test-xml-stream) test-xml-element)))

(deftest test-element-name
  (is (= (element-name test-xml-element) :test))
  (is (nil? (element-name nil))))

(deftest test-attributes
  (is (= (attributes test-xml-element) foo-attributes))
  (is (nil? (attributes nil))))

(deftest test-content
  (is (= (content test-xml-element) test-xml-content))
  (is (nil? (content nil))))

(deftest test-attribute-value
  (is (= (attribute-value test-xml-element :foo) "bar"))
  (is (nil? (attribute-value test-xml-element :fail)))
  (is (nil? (attribute-value test-xml-element nil)))
  (is (nil? (attribute-value nil :foo)))
  (is (nil? (attribute-value nil nil))))

(deftest test-element?
  (is (element? test-xml-element))
  (is (not (element? {})))
  (is (not (element? nil))))

(deftest test-child-elements
  (is (= (child-elements test-xml-element) [child-element]))
  (is (= (child-elements child-element) []))
  (is (= (child-elements nil) [])))

(deftest test-text
  (is (= (text test-xml-element) ["text"]))
  (is (= (text child-element) ["child text"]))
  (is (= (text (struct xml/element :textTest nil [])) []))
  (is (= (text (struct xml/element :textTest nil nil)) []))
  (is (= (text nil) [])))

(deftest test-to-element-name
  (is (= (to-element-name :test) :test))
  (is (= (to-element-name "test") :test))
  (is (= (to-element-name 'test) :test))
  (is (nil? (to-element-name nil))))

(deftest test-is-element?
  (is (is-element? test-xml-element :test))
  (is (not (is-element? child-element :test)))
  (is (not (is-element? nil :test)))
  (is (not (is-element? test-xml-element nil)))
  (is (not (is-element? nil nil))))

(deftest test-find-children
  (is (= (find-children test-xml-element :child) [child-element]))
  (is (= (find-children test-xml-element :fail) []))
  (is (= (find-children test-xml-element nil) []))
  (is (= (find-children nil :child) []))
  (is (= (find-children nil nil) [])))

(deftest test-find-child
  (is (= (find-child test-xml-element :child) child-element))
  (is (nil? (find-child test-xml-element :fail)))
  (is (nil? (find-child test-xml-element nil)))
  (is (nil? (find-child nil :child)))
  (is (nil? (find-child nil nil))))