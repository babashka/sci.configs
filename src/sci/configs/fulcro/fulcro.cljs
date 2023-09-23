(ns sci.configs.fulcro.fulcro
  "The root of all SCI configuration for Fulcro.
   
   == Example

   ```clj
   (ns demo 
     (:require [sci.core :as sci]
               [sci.configs.fulcro.fulcro :as fulcro]))
   (def sci-ctx (doto (sci/init {}) (sci/merge-opts fulcro/config)))
   (sci/eval-string* sci-ctx
     \"(ns page
         (:require
           [com.fulcrologic.fulcro.application :as app]
           [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
           [com.fulcrologic.fulcro.dom :as dom]))
       (defsc Root [this props] (dom/h3 \\\"Hello from Fulcro!\\\"))
       (let [app (app/fulcro-app {})]
         (app/mount! app Root \\\"sciapp\\\"))\")
   ```
   
   == Status
   
   Early alpha. Many namespaces aren't exposed yet, and there are certainly
   bugs in how macros were ported to SCI."
  (:require [sci.configs.fulcro.algorithms.data-targeting :as dt]
            [sci.configs.fulcro.algorithms.denormalize :as fdn]
            [sci.configs.fulcro.algorithms.form-state :as fs]
            [sci.configs.fulcro.algorithms.lookup :as ah]
            [sci.configs.fulcro.algorithms.merge :as merge]
            [sci.configs.fulcro.algorithms.normalize :as fnorm]
            [sci.configs.fulcro.algorithms.react-interop :as interop]
            [sci.configs.fulcro.algorithms.tempid :as tempid]
            [sci.configs.fulcro.algorithms.tx-processing.synchronous-tx-processing :as stx]
            [sci.configs.fulcro.application :as app]
            [sci.configs.fulcro.component :as comp]
            [sci.configs.fulcro.data-fetch :as df]
            [sci.configs.fulcro.dom :as dom]
            [sci.configs.fulcro.mutations :as m]
            [sci.configs.fulcro.networking.http-remote :as http-remote]
            [sci.configs.fulcro.raw.component :as rc]
            [sci.configs.fulcro.react.hooks :as hooks]
            [sci.configs.fulcro.routing.dynamic-routing :as dr]
            [sci.configs.fulcro.ui-state-machines :as uism]
            [sci.core :as sci]
            [edn-query-language.core]))

(def eql-sci-ns (sci/create-ns 'edn-query-language.core))
(def eql-ns-def (sci/copy-ns edn-query-language.core eql-sci-ns {}))

(def namespaces
  (merge
   {'edn-query-language.core eql-ns-def}
   ah/namespaces
   app/namespaces
   comp/namespaces
   df/namespaces
   dom/namespaces
   dr/namespaces
   dt/namespaces
   fdn/namespaces
   fnorm/namespaces
   fs/namespaces
   http-remote/namespaces
   interop/namespaces
   merge/namespaces
   m/namespaces
   rc/namespaces
   hooks/namespaces
   stx/namespaces
   tempid/namespaces
   uism/namespaces))

(def config {:namespaces namespaces})
