;; Adapted from https://github.com/babashka/babashka/blob/f3fa33ae29f852ed47bfbe7e07b0b0716622ccba/test/babashka/datafy_test.clj

(ns clojure.core.datafy-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [sci.core :as sci]
            [sci.configs.clojure.datafy :as datafy-namespace]
            [sci.configs.clojure.core.protocols :as protocols-namespace]))

(defn ctx-fn [] (sci/init {:namespaces datafy-namespace/namespaces}))


(defn sci [s]
  (let [ctx (ctx-fn)]
    ; (println "Evalling" s)
    (sci/eval-string* ctx s)))

(deftest datafy-test
  (testing "default implementation of datafy works"
    (is (= #{:public} (sci "(require '[clojure.datafy :as d]) (:flags (d/datafy Exception))"))))
  (testing "custom implementation of datafy works"
    (is (= {:number 1} (sci "
(require '[clojure.datafy :as d]
         '[clojure.core.protocols :as p])

(extend-type Number
  p/Datafiable
  (datafy [x]
    {:number x}))

(d/datafy 1)
"))))
  (testing "implement datafy via metadata"
    (is (= {:datafied []} (sci "
(require '[clojure.datafy :as d]
         '[clojure.core.protocols :as p])

(def x (with-meta [] {`p/datafy (fn [this] {:datafied this})}))
(d/datafy x)
"))))
  (testing "reify Datafiable"
    (let [prog "
(require '[clojure.datafy :as d]
         '[clojure.core.protocols :as p])

(def x (reify p/Datafiable (datafy [_] [:data])))
(d/datafy x)"]
      (is (= [:data] (sci prog)))))

  (testing "default implementation of nav works"
    (is (= 1 (sci "(require '[clojure.datafy :as d]) (d/nav {:a 1} :a 1)"))))
  (testing "custom implementation of nav works"
    (is (= \f (sci "
(require '[clojure.datafy :as d]
         '[clojure.core.protocols :as p])

(extend-type String
  p/Navigable
  (nav [coll k v]
    (.charAt coll k)))

(d/nav \"foo\" 0 nil)
"))))
  (testing "implement nav via metadata"
    (is (= {:nav [[] :k :v]} (sci "
(require '[clojure.datafy :as d]
         '[clojure.core.protocols :as p])

(def x (with-meta [] {`p/nav (fn [this k v] {:nav [this k v]})}))
(d/nav x :k :v)
"))))
  (testing "reify Navigable"
    (let [prog "
(require '[clojure.datafy :as d]
         '[clojure.core.protocols :as p])

(def x (reify p/Navigable (nav [_ _ _] [:data])))
(d/nav x nil nil)"]
      (is (= [:data] (sci prog))))))

;;;; Scratch
(comment
  (t/run-tests *ns*)
  (datafy-test))