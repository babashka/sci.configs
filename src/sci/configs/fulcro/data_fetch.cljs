(ns sci.configs.fulcro.data-fetch
  (:require
   [sci.core :as sci]
   com.fulcrologic.fulcro.data-fetch))


(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.data-fetch))
(def ns-def (sci/copy-ns com.fulcrologic.fulcro.data-fetch sci-ns {:exclude ['render-to-str]}))

(def namespaces {'com.fulcrologic.fulcro.data-fetch ns-def})