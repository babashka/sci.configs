# SCI configs

A collection of SCI configuration for the following libraries:

* js-interop
* reagent

This repository only provides the config on the classpath and library
consumers are meant to declare their own dependencies.

In general, a configuration can be enabled as in the following example for `applied-science.js-interop`.

``` clojure
(ns example
  (:require [sci-configs.applied-science.js-interop.config :as js-interop-config]
            [sci.core :as sci]))

(def sci-ctx (doto (sci/init {}) ;; your initial config here
               (sci/merge-opts js-interop-config/config)))

(sci/eval-string* sci-ctx
  "(require '[applied-science.js-interop :as j]) (j/assoc! #js {} :foo 1)")
```
