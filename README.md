# SCI configs

A collection of ready to be used SCI configs.

This repository provides SCI configurations on the classpath. See the `:dev`
alias to check against which version of the libraries the configurations are
tested. You should bring in the dependency of targeted libraries yourself.

In general, a configuration can be enabled as in the following example for `applied-science.js-interop`:

``` clojure
(ns example
  (:require [sci.configs.applied-science.js-interop :as j]
            [sci.core :as sci]))

(def sci-ctx (doto (sci/init {}) ;; your initial config here
               (sci/merge-opts j/config)))

(sci/eval-string* sci-ctx
  "(require '[applied-science.js-interop :as j]) (j/assoc! #js {} :foo 1)")
;;=> #js {:foo 1}
```

or if you like to include the config without calling `sci/merge-opts` (e.g. for
performance reasons), use the per-namespace values directly:

``` clojure
(ns example
  (:require [sci.configs.applied-science.js-interop :as j]
            [sci.core :as sci]))

(def sci-ctx (sci/init {:namespaces {'applied-science.js-interop j/js-interop-namespace}}))
```

## API

In general, only `library/*-namespace` and `library/config` vars are intented as the
public API. The rest is subject to breakage, even when vars are public. For
convenience, we list the public API for each library here.

## Libraries

### [applied-science/js-interop](https://github.com/applied-science/js-interop)

Namespace: `sci.configs.applied-science.js-interop`

Public API:

- `config`
- `js-interop-namespace`

### [funcool/promesa](https://github.com/funcool/promesa)

Namespace: `sci.configs.funcool.promesa`

Public API:

- `config`
- `promesa-namespace`
- `promesa-protocols-namespace`

### [reagent/reagent](https://github.com/reagent-project/reagent)

Namespace: `sci.configs.reagent.reagent`

Public API:

- `config`
- `reagent-namespace`
- `reagent-ratom-namespace`
- `reagent-debug-namespace`

The configuration for `reagent.dom.server` is available seperately via:

Namespace: `sci.configs.reagent.reagent-dom-server`

Public API:

- `config`
- `reagent-dom-server-namespace`

## License

The configurations are licensed under the same licenses as the libraries they
target. You are free to take the configs from this repository and adapt them as
necessary for your projects. See LICENSE for additional info.
