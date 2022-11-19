(ns sci.configs.re-frame.re-frame
  (:require
   [re-frame.core]
   [sci.core :as sci]))

(def rfns (sci/create-ns 're-frame.core nil))

(def re-frame-namespace
  (sci/copy-ns re-frame.core rfns))

(def namespaces
  {'re-frame.core re-frame-namespace})

(def config
  {:namespaces namespaces})
