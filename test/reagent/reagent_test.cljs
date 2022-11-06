(ns reagent.reagent-test
  (:require
   [cljs.test :refer [deftest is]]
   [sci.configs.reagent.reagent :as reagent-config]
   [sci.configs.reagent.reagent-dom-server :as dom-server-config]
   [sci.core :as sci]))

(defn ctx-fn [] (sci/init {:namespaces (into reagent-config/namespaces
                                             dom-server-config/namespaces)}))

(deftest function?-test
  (let [ctx (ctx-fn)]
    (is (= "<div>1</div>"
           (sci/eval-string* ctx "
(ns example
  (:require
    [reagent.core :as r]
    [reagent.dom.server :as rdom]))
(def click-count (r/atom 0))

(defn counting-component []
  [:div @click-count])

(swap! click-count inc)

(rdom/render-to-string [counting-component])")))))
