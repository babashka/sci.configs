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
- `namespaces`

### [com.fulcrologic/fulcro](https://github.com/fulcro/fulcro)

Namespace: `sci.configs.fulcro.fulcro`

Public API:

- `config`
- `namespaces`

### [funcool/promesa](https://github.com/funcool/promesa)

Namespace: `sci.configs.funcool.promesa`

Public API:

- `config`
- `promesa-namespace`
- `promesa-protocols-namespace`
- `namespaces`

### [reagent/reagent](https://github.com/reagent-project/reagent)

Namespace: `sci.configs.reagent.reagent`

Public API:

- `config`
- `reagent-namespace`
- `reagent-ratom-namespace`
- `reagent-debug-namespace`
- `namespaces`

The configuration for `reagent.dom.server` is available seperately via:

Namespace: `sci.configs.reagent.reagent-dom-server`

Public API:

- `config`
- `reagent-dom-server-namespace`

### [re-frame/re-frame](https://github.com/day8/re-frame)

Namespace: `sci.configs.re-frame.re-frame`

Public API:

- `config`
- `re-frame-namespace`
- `re-frame-db-namespace`
- `namespaces`

The configuration for `re-frame.alpha` is available seperately via:

Namespace: `sci.configs.re-frame.re-frame-alpha`

Public API:

- `config`
- `re-frame-alpha-namespace`


### [mfikes/cljs-bean](https://github.com/mfikes/cljs-bean)

Namespace: `sci.configs.mfikes.cljs-bean`

Public API:

- `config`
- `namespaces`
- `cljs-bean-namespace`

### [cljs.test](https://cljs.github.io/api/cljs.test/)

Namespace `sci.configs.cljs.test`

Public API:

- `config`
- `namespaces`
- `cljs-test-namespace`

### [cljs.pprint](https://cljs.github.io/api/cljs.pprint/)

Public API:

- `config`
- `namespaces`
- `cljs-pprint-namespace`

### [tonsky/datascript](https://github.com/tonsky/datascript)

Public API:

- `config`
- `namespaces`
- `core-namespace`
- `db-namespace`

### [clojure-1-11](https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj)

Namespace: `clojure.core`

New functions added to clojure.core in version 1.11

- `namespaces`

## Contributing

`npm install` and `bb test`

### Development

You can play with your SCI code and configs in a cljs REPL. In Calva, run Jack-in to a shadow-cljs repl and choose the `:dev` build. Elsewhere, run `bb dev` and then connect to its nrepl at port 9000. Access the web page that Shadow serves at http://localhost:8081/ and then eval your code using `development.cljs`.

## License

The configurations are licensed under the same licenses as the libraries they
target. You are free to take the configs from this repository and adapt them as
necessary for your projects. See LICENSE for additional info.
