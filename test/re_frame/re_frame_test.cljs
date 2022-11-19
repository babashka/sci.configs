(ns re-frame.re-frame-test
  (:require
   [cljs.test :refer [deftest is]]
   [sci.configs.re-frame.re-frame :as re-frame-config]
   [sci.core :as sci]))

(defn ctx-fn [] (sci/init {:namespaces re-frame-config/namespaces}))

(deftest dispatch-test
  (let [ctx (ctx-fn)]
    (is (= 1
           (sci/eval-string* ctx "(ns example
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-db
  :initialize
  (fn [db _]
    {:count 0}))

(rf/reg-sub
  :count
  (fn [db]
    (:count db)))

(rf/reg-event-db
  :inc
  (fn [db _]
    (update db :count inc)))

(rf/dispatch-sync [:initialize])

(rf/dispatch-sync [:inc])

@(rf/subscribe [:count])
")))))
