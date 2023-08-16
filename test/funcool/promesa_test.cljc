(ns funcool.promesa-test
  (:require
   [cljs.test :refer [deftest is async]]
   [sci.configs.funcool.promesa :as promesa-config]
   [sci.core :as sci]
   [promesa.core :as p]))

(defn ctx-fn [] (sci/init {:namespaces promesa-config/namespaces}))

(deftest future-test
  (let [ctx (ctx-fn)
        p (sci/eval-string* ctx "
(ns example
  (:require
    [promesa.core :as p]))

(p/do
1 2 3
(p/let [x (p/resolved (inc 2))
        y (inc x)]
  (inc y)))")]
    (async done
           (-> p
               (.then (fn [v]
                        (is (= 5 v))))
               (.catch (fn [_] (is false)))
               (.finally done)))))
