(ns sci.configs.fulcro.react.version18
  (:require [sci.core :as sci]
            com.fulcrologic.fulcro.react.version18))
 
(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.react.version18))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.react.version18 sci-ns))
  
(def namespaces {'com.fulcrologic.fulcro.react.version18 ns-def})