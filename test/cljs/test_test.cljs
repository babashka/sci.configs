(ns cljs.test-test
  (:require
   [cljs.test :refer [deftest]]
   [sci.configs.cljs.test :as t]
   [sci.core :as sci]))

(def ctx (sci/init {:namespaces {'cljs.test t/cljs-test-namespace}}))

(deftest test-test
  (prn (sci/eval-string* ctx "
(require '[cljs.test :as t :refer [deftest is testing]])
(deftest foo
  (is (= 1 2)))")))
