(ns sci.configs.fulcro.algorithms.merge
  (:require
   [sci.core :as sci]
   com.fulcrologic.fulcro.algorithms.merge))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.merge))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.algorithms.merge sci-ns))

(def namespaces {'com.fulcrologic.fulcro.algorithms.merge ns-def})