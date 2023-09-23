(ns development
  "Entry point for code loaded by shadow-cljs"
  (:require
   [sci.core :as sci]
   [sci.configs.fulcro.fulcro :as fulcro-config]))

;; Necessary to avoid the error 'Attempting to call unbound fn: #'clojure.core/*print-fn*'
;; when calling `println` inside the evaluated code
(enable-console-print!)
(sci/alter-var-root sci/print-fn (constantly *print-fn*))
(sci/alter-var-root sci/print-err-fn (constantly *print-err-fn*))

(def full-ctx (doto (sci/init {})
                (sci/merge-opts fulcro-config/config)))

(defn init []
  (println "Init run"))

(defn reload []
  (println "Reload run"))

(comment
  (sci/eval-string* (sci/init {}) "(+ 1 2)")
  
  (sci/eval-string* full-ctx "
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
  (dom/render-to-str ((comp/factory Root) (build-ui-tree))))
")
  ,)
