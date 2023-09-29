(ns sci.configs.fulcro.networking.http-remote
  (:require [sci.core :as sci]
            com.fulcrologic.fulcro.networking.http-remote))
  
  (def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.networking.http-remote))
  (def ns-def (sci/copy-ns com.fulcrologic.fulcro.networking.http-remote sci-ns))
  
  (def namespaces {'com.fulcrologic.fulcro.networking.http-remote ns-def})