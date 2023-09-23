(ns sci.configs.fulcro.application
  (:require
   [sci.core :as sci]
   [com.fulcrologic.fulcro.application]))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.application))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.application sci-ns))

(def namespaces {'com.fulcrologic.fulcro.application ns-def})