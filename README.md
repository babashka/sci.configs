# SCI configs

A collection of SCI configuration for the following libraries:

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

## API

In general, only `library/*-namespace` and `library/config` vars are intented as the
public API. The rest is subject to breakage, even when vars are public. For
convenience, we list the public API for each library here.

The configurations are licensed under the same licenses as the libraries they target.

## Libraries

### [applied-science/js-interop](https://github.com/applied-science/js-interop)

Namespace: `sci-configs.reagent.reagent`

Public API:

- `config`
- `js-interop-namespace`

### [reagent/reagent](https://github.com/reagent-project/reagent)

Namespace: `sci-configs.reagent.reagent`

Public API:

- `config`
- `reagent-namespace`
- `reagent-ratom-namespace`
- `reagent-debug-namespace`

## License
