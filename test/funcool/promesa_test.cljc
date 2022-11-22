(ns funcool.promesa-test
  (:require #?(:cljs [cljs.test :as t]
               :clj [clojure.test :as t])
            [promesa.tests.util :refer [promise-ok promise-ko normalize-to-value]]
            [promesa.core :as p :include-macros true]
            [promesa.exec :as e]))

(t/deftest future-macro
  (let [p1 (p/future (+ 1 2 3))
        test #(p/then p1 (fn [res] (t/is (= res 6))))]
    #?(:cljs (t/async done (p/do (test) (done)))
       :clj @(test))))

(t/deftest loop-and-recur
  (let [p1 (p/loop [a (p/delay 50 0)]
             (if (= a 5)
               a
               (p/recur (p/delay 50 (inc a)))))
        test #(->> (p/race [p1 (p/delay 400 10)])
                   (p/map (fn [res] (t/is (= res 5)))))]
    #?(:cljs (t/async done (p/do (test) (done)))
       :clj @(test))))

;; --- Threading tests

(defn future-inc [x]
  (p/future (inc x)))

(t/deftest doseq-test
  (let [test #(p/let [state (atom [])
                      xs [10 20 30]]
                (p/doseq [x xs]
                  (p/delay (- 100 x))
                  (swap! state conj x))
                (t/is (= xs @state)))]
    #?(:cljs (t/async done (p/do (test) (done)))
       :clj @(test))))
