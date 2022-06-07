(ns sci.configs.mfikes.cljs-bean
  (:require
   [cljs-bean.core :as bean]
   [sci.core :as sci]))

(def bns (sci/create-ns 'cljs-bean.core nil))

(def cljs-bean-namespace
  {'bean (sci/copy-var bean/bean bns)
   'bean? (sci/copy-var bean/bean? bns)
   'object (sci/copy-var bean/object bns)
   '->js (sci/copy-var bean/->js bns)
   '->clj (sci/copy-var bean/->clj bns)})

(def namespaces {'cljs-bean.core cljs-bean-namespace})
(def config {:namespaces namespaces})
