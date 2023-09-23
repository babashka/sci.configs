(ns sci.configs.fulcro.algorithms.tempid
  (:require [sci.core :as sci]
            com.fulcrologic.fulcro.algorithms.tempid))
  
  (def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.tempid))
  (def ns-def (sci/copy-ns com.fulcrologic.fulcro.algorithms.tempid sci-ns))
  
  (def namespaces {'com.fulcrologic.fulcro.algorithms.tempid ns-def})