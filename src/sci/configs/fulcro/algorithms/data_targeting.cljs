(ns sci.configs.fulcro.algorithms.data-targeting
  (:require [sci.core :as sci]
            [com.fulcrologic.fulcro.algorithms.data-targeting]))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.data-targeting))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.algorithms.data-targeting sci-ns))

(def namespaces {'com.fulcrologic.fulcro.algorithms.data-targeting ns-def})