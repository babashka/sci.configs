(ns sci.configs.fulcro.algorithms.denormalize
  (:require
   [sci.core :as sci]
   com.fulcrologic.fulcro.algorithms.denormalize))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.denormalize))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.algorithms.denormalize sci-ns))

(def namespaces {'com.fulcrologic.fulcro.algorithms.denormalize ns-def})