(ns sci.configs.cjohansen.replicant
  (:require [replicant.alias :as ra]
            [replicant.dom :as rd]
            [replicant.string :as rs]
            [sci.core :as sci]))

(def rdns (sci/create-ns 'replicant.dom nil))

(def replicant-dom-namespace
  {'render (sci/copy-var rd/render rdns)
   'unmount (sci/copy-var rd/unmount rdns)
   'set-dispatch! (sci/copy-var rd/set-dispatch! rdns)})

(def rsns (sci/create-ns 'replicant.string nil))

(def replicant-string-namespace
  {'create-renderer (sci/copy-var rs/create-renderer rsns)
   'render (sci/copy-var rs/render rsns)})

(def rans (sci/create-ns 'replicant.alias nil))

(def replicant-alias-namespace
  {'register! (sci/copy-var ra/register! rsns)})

(def namespaces {'replicant.dom replicant-dom-namespace
                 'replicant.string replicant-string-namespace
                 'replicant.alias replicant-alias-namespace})

(def config {:namespaces namespaces})
