(ns sci.configs.fulcro.algorithms.lookup
  (:require [sci.core :as sci]
            com.fulcrologic.fulcro.algorithms.lookup))


(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.lookup))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.algorithms.lookup sci-ns))

(def namespaces {'com.fulcrologic.fulcro.algorithms.lookup ns-def})