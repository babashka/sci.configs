(ns funcool.promesa-test
  (:require
   [cljs.test :refer [deftest is async]]
   [sci.configs.funcool.promesa :as promesa-config]
   [sci.core :as sci]))

(defn ctx-fn [] (sci/init {:namespaces promesa-config/namespaces}))

(deftest future-test
  (let [ctx (ctx-fn)
        p (sci/eval-string* ctx "
(ns example
  (:require
    [promesa.core :as p]))

(p/future (inc 2))")]
    (async done
           (-> (.then p (fn [v] (is (= 3 v))))
               (.catch (fn [_] (is false)))
               (.finally (done))))))

(deftest promise-done?
  (let [p1 (p/promise 1)]
    (is (p/done? p1))))
