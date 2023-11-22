(ns sci.configs.re-frame.re-frame-alpha
  (:require
   [re-frame.alpha]
   [sci.core :as sci]))

(def rfa (sci/create-ns 're-frame.alpha nil))

(def re-frame-alpha-namespace
  (sci/copy-ns re-frame.alpha rfa))

(def namespaces
  {'re-frame.alpha re-frame-alpha-namespace})

(def config
  {:namespaces namespaces})
