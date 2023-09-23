(ns fulcro.fulcro-test
  (:require
   [cljs.test :refer [deftest is]]
   [sci.configs.fulcro.fulcro :as fulcro-config]
   [sci.core :as sci]))

(defn ctx-fn [] (sci/init fulcro-config/config))

(deftest simple-component-test
  (let [ctx (ctx-fn)]
    (is (= "<h3>Hello from Fulcro!</h3>"
           (sci/eval-string* ctx "
(ns test1
  (:require
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]))
                                  
(defsc Root [this props] (dom/h3 \"Hello from Fulcro!\"))
(defn build-ui-tree []
  (let [client-db (comp/get-initial-state Root {})]
    (fdn/db->tree (comp/get-query Root client-db) client-db client-db)))
(comp/with-parent-context (app/fulcro-app)
  (dom/render-to-str ((comp/factory Root) (build-ui-tree))))")))))
