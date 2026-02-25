;; Adapted from https://github.com/babashka/babashka/blob/f3fa33ae29f852ed47bfbe7e07b0b0716622ccba/src/babashka/impl/protocols.clj#L4

(ns sci.configs.clojure.datafy
  (:require
   [sci.configs.clojure.core.protocols :as protocols]
   [sci.core :as sci]))

(def datafy-ns (sci/create-ns 'clojure.datafy nil))

(def datafy-namespace
  {'datafy (sci/copy-var protocols/datafy datafy-ns)
   'nav (sci/copy-var protocols/nav datafy-ns)})