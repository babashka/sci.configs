(ns sci.configs.fulcro.algorithms.normalize
  (:require [sci.core :as sci]
            com.fulcrologic.fulcro.algorithms.normalize))


(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.normalize))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.algorithms.normalize sci-ns))

(def namespaces {'com.fulcrologic.fulcro.algorithms.normalize ns-def})