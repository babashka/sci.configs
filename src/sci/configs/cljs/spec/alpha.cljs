(ns sci.configs.cljs.spec.alpha
  (:refer-clojure :exclude [defmacro and])
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [sci.core :as sci]
            [sci.ctx-store :as ctx]
            [clojure.walk :as walk])
  (:require-macros [sci.configs.macros :as macros :refer [defmacro]]))

(def sns (sci/create-ns 'cljs.spec.alpha nil))

(defonce ^:private registry-ref (atom {}))
(defonce ^:private _speced_vars (atom #{}))

(defn- unfn [expr]
  (if (clojure.core/and (seq? expr)
                        (symbol? (first expr))
                        (= "fn*" (name (first expr))))
    (let [[[s] & form] (rest expr)]
      (conj (walk/postwalk-replace {s '%} form) '[%] 'cljs.core/fn))
    expr))

(defn- ->sym
  "Returns a symbol from a symbol or var"
  [x]
  (if (map? x)
    (:name x)
    x))

(defn- res [env form]
  (cond
    (keyword? form) form
    (symbol? form) (clojure.core/or (->> form (sci/resolve env) ->sym) form)
    (sequential? form) (walk/postwalk #(if (symbol? %) (res env %) %) (unfn form))
    :else form))

(defn- ns-qualify
  "Qualify symbol s by resolving it or using the current *ns*."
  [_env s]
  (if (namespace s)
    (->sym (sci/resolve (ctx/get-ctx) s))
    (symbol (str @sci/ns) (str s))))

(macros/defmacro def*
  "Given a namespace-qualified keyword or resolveable symbol k, and a
  spec, spec-name, predicate or regex-op makes an entry in the
  registry mapping k to the spec. Use nil to remove an entry in
  the registry for k."
  [k spec-form]
  (let [&env (ctx/get-ctx)
        k (if (symbol? k)
            (let [sym (ns-qualify &env k)]
              (swap! _speced_vars conj
                     (vary-meta sym assoc :fdef-ns (-> &env :ns :name)))
              sym)
            k)
        form (res &env spec-form)]
    (swap! registry-ref (fn [r]
                          (if (nil? form)
                            (dissoc r k)
                            (assoc r k form))))
    `(cljs.spec.alpha/def-impl '~k '~form ~spec-form)))

(macros/defmacro and
  "Takes predicate/spec-forms, e.g.

  (s/and even? #(< % 42))

  Returns a spec that returns the conformed value. Successive
  conformed values propagate through rest of predicates."
  [& pred-forms]
  (let [&env (ctx/get-ctx)]
    `(cljs.spec.alpha/and-spec-impl '~(mapv #(res &env %) pred-forms) ~(vec pred-forms) nil)))

(def namespaces {'cljs.spec.alpha {'def (sci/copy-var def* sns)
                                   'def-impl (sci/copy-var s/def-impl sns)
                                   'and (sci/copy-var and sns)
                                   'and-spec-impl (sci/copy-var s/and-spec-impl sns)
                                   'valid? (sci/copy-var s/valid? sns)}})

(def config {:namespaces namespaces})
