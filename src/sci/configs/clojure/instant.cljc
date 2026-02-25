;; Adapted from https://github.com/babashka/babashka/blob/f3fa33ae29f852ed47bfbe7e07b0b0716622ccba/src/babashka/impl/clojure/instant.clj

(ns sci.configs.clojure.instant
  (:require [clojure.instant :as i]
            [sci.core :as sci]))

(def ins (sci/create-ns 'clojure.instant nil))

(def instant-namespace
  {'read-instant-date (sci/copy-var i/read-instant-date ins)
   'parse-timestamp (sci/copy-var i/parse-timestamp ins)})
   
(def namespaces {'clojure.instant instant-namespace})

(def config {:namespaces namespaces})