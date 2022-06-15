(ns cljs.test-test
  (:require
   [cljs.test :refer [deftest is testing]]
   [sci.configs.cljs.test :as t]
   [sci.core :as sci]))

(def ctx (sci/init {:namespaces {'cljs.test t/cljs-test-namespace}}))

(deftest function?-test
  (is (true? (sci/eval-string* ctx "
(require '[cljs.test :as t])
(t/function? 'inc)"))))

(deftest is-test
  (let [output (atom "")]
    (sci/binding [sci/print-fn (fn [s]
                                 (swap! output str s))]
      (sci/eval-string* ctx "
(ns foo)
(require '[cljs.test :as t :refer [deftest is testing]])
(deftest foo
  (is (= 1 1)))
(cljs.test/run-tests 'foo)"))
    (prn @output)))

(deftest deftest-test
  (let [output (atom "")]
    (sci/binding [sci/print-fn (fn [s]
                                 (swap! output str s))]
      (sci/eval-string* ctx "
(ns foo)
(require '[cljs.test :as t :refer [deftest is testing]])
#_(deftest foo
  (is (= 1 1)))
#_(foo)
(is (= 1 1))
#_(cljs.test/test-var 'foo/foo)"))
    (prn @output)))

(defmulti foo
  (fn [ctx a b] a))

(defmethod foo :a [ctx a b]
  [(keys ctx) a b])


