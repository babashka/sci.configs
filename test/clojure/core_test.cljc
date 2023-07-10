(ns clojure.core-test
  (:require
   [clojure.test :refer [deftest is]]
   [sci.configs.clojure.core :as core-namespace]
   [sci.core :as sci]))

(defn ctx-fn [] (sci/init {:namespaces core-namespace/version-1-11}))

(deftest parse-functions-test
  (let [ctx (ctx-fn)]
    (is (= 1 (sci/eval-string* ctx "(parse-long \"1\")")))
    (is (= 1.5 (sci/eval-string* ctx "(parse-double \"1.5\")")))
    (is (= true (sci/eval-string* ctx "(parse-boolean \"true\")")))
    (is (= "00000000-0000-0000-0000-000000000000" (sci/eval-string* ctx "(str (parse-uuid \"00000000-0000-0000-0000-000000000000\"))")))))

(deftest random-uuid-test
  (let [ctx (ctx-fn)]
    (is (= true (sci/eval-string* ctx "(uuid? (random-uuid \"00000000-0000-0000-0000-000000000000\"))")))))

(deftest update-keys-test
  (let [ctx (ctx-fn)]
    (is (= {:a "1"} (sci/eval-string* ctx "(update-keys {\"a\" \"1\"} keyword)")))))
(deftest update-vals-test
  (let [ctx (ctx-fn)]
    (is (= {"a" :1} (sci/eval-string* ctx "(update-vals {\"a\" \"1\"} keyword)")))))

(deftest abs-test
  (let [ctx (ctx-fn)]
    (is (= 1 (sci/eval-string* ctx "(abs -1)")))))

(deftest infinite?-test
  (let [ctx (ctx-fn)]
    (is (= true (sci/eval-string* ctx "(infinite? (/ 1.0 0))")))))

(deftest NaN?-test
  (let [ctx (ctx-fn)]
    (is (= true (sci/eval-string* ctx "(NaN? (/ 0.0 0))")))))

(deftest iteration-test
  (let [ctx (ctx-fn)]
    (is (= [1 2 3 4 5] (sci/eval-string* ctx "(vec (iteration identity {:somef #(< % 6) :kf inc :initk 1}))")))))