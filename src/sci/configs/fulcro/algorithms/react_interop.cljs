(ns sci.configs.fulcro.algorithms.react-interop
  (:require [sci.core :as sci]
            com.fulcrologic.fulcro.algorithms.react-interop))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.algorithms.react-interop))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.algorithms.react-interop sci-ns))

(def namespaces {'com.fulcrologic.fulcro.algorithms.react-interop ns-def})