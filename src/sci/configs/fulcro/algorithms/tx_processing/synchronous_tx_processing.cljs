(ns sci.configs.fulcro.algorithms.tx-processing.synchronous-tx-processing
  (:require [sci.core :as sci]
            [com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing :as stx]))
  
  (defn ^:sci/macro in-transaction [_&form _&env app-sym & body]
    `(let [id# (:com.fulcrologic.fulcro.application/id ~app-sym)]
       (swap! stx/apps-in-tx update id# conj (stx/current-thread-id))
       (try
         ~@body
         (finally
           (swap! apps-in-tx update id# pop)))))

  (def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing))
  (def ns-def (assoc (sci/copy-ns com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing sci-ns
                                  {:exclude [in-transaction]})
                     'in-transaction (sci/copy-var in-transaction sci-ns)))
  
  (def namespaces {'com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing ns-def})