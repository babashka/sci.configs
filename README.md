# SCI configs

A collection of SCI configuration for the following libraries:

* [applied-science/js-interop](https://github.com/applied-science/js-interop)
* [reagent](https://github.com/reagent-project/reagent)

This repository only provides the config on the classpath and library consumers
are meant to declare their own dependencies.

See the `:dev` alias to check against which version of the libraries the
configurations are tested.

In general, a configuration can be enabled as in the following example for `applied-science.js-interop`:

``` clojure
(ns example
  (:require [sci-configs.applied-science.js-interop :as j]
            [sci.core :as sci]))

(def sci-ctx (doto (sci/init {}) ;; your initial config here
               (sci/merge-opts j/config)))

(sci/eval-string* sci-ctx
  "(require '[applied-science.js-interop :as j]) (j/assoc! #js {} :foo 1)")
;;=> #js {:foo 1}
```

or if you like to include the config in advanced (e.g. for performance reasons), use the per-namespace values:

``` clojure
(ns example
  (:require [sci-configs.applied-science.js-interop :as j]
            [sci.core :as sci]))

(def sci-ctx (sci/init {:namespaces {'applied-science.js-interop j/js-interop-namespace}}))
```
