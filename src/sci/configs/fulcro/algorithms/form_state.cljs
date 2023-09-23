(ns sci.configs.fulcro.algorithms.form-state
  (:require [sci.core :as sci]
            com.fulcrologic.fulcro.algorithms.form-state))
  
  (def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.form-state))
  (def ns-def (sci/copy-ns com.fulcrologic.fulcro.algorithms.form-state sci-ns))
  
  (def namespaces {'com.fulcrologic.fulcro.algorithms.form-state ns-def})