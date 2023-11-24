(ns sci.configs.reagent.reagent-dom-client
  (:require [reagent.dom.client :as srv]
            [sci.core :as sci]))

(def rdcns (sci/create-ns 'reagent.dom.client nil))

(def reagent-dom-client-namespace
  {'render (sci/copy-var rdc/render rdcns)
   'create-root (sci/copy-var rdc/create-root rdcns)})

(def namespaces {'reagent.dom.client reagent-dom-client-namespace})

(def config {:namespaces namespaces})
