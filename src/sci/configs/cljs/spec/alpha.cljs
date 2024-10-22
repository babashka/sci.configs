(ns sci.configs.cljs.spec.alpha
  (:refer-clojure :exclude [and or])
  (:require [clojure.spec.alpha :as s]
            [sci.core :as sci]
            [sci.ctx-store :as ctx]
            [clojure.walk :as walk])
  (:require-macros [sci.configs.macros :as macros]))

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
    `(s/def-impl '~k '~form ~spec-form)))

(macros/defmacro and
  "Takes predicate/spec-forms, e.g.

  (s/and even? #(< % 42))

  Returns a spec that returns the conformed value. Successive
  conformed values propagate through rest of predicates."
  [& pred-forms]
  (let [&env (ctx/get-ctx)]
    `(s/and-spec-impl '~(mapv #(res &env %) pred-forms) ~(vec pred-forms) nil)))

(macros/defmacro or
  "Takes key+pred pairs, e.g.

  (s/or :even even? :small #(< % 42))

  Returns a destructuring spec that returns a map entry containing the
  key of the first matching pred and the corresponding value. Thus the
  'key' and 'val' functions can be used to refer generically to the
  components of the tagged return."
  [& key-pred-forms]
  (let [&env (ctx/get-ctx)
        pairs (partition 2 key-pred-forms)
        keys (mapv first pairs)
        pred-forms (mapv second pairs)
        pf (mapv #(res &env %) pred-forms)]
    (clojure.core/assert (clojure.core/and (even? (count key-pred-forms)) (every? keyword? keys)) "spec/or expects k1 p1 k2 p2..., where ks are keywords")
    `(s/or-spec-impl ~keys '~pf ~pred-forms nil)))

(macros/defmacro nilable
  "returns a spec that accepts nil and values satisfiying pred"
  [pred]
  (let [&env (ctx/get-ctx)
        pf (res &env pred)]
    `(s/nilable-impl '~pf ~pred nil)))

(def namespaces {'cljs.spec.alpha {'def (sci/copy-var def* sns)
                                   'def-impl (sci/copy-var s/def-impl sns)
                                   'and (sci/copy-var and sns)
                                   'and-spec-impl (sci/copy-var s/and-spec-impl sns)
                                   'or (sci/copy-var or sns)
                                   'or-spec-impl (sci/copy-var s/or-spec-impl sns)
                                   'valid? (sci/copy-var s/valid? sns)
                                   'conform (sci/copy-var s/conform sns)
                                   'nilable (sci/copy-var nilable sns)
                                   'nilable-impl (sci/copy-var s/nilable-impl sns)
                                   'explain (sci/copy-var s/explain sns)}})

(def config {:namespaces namespaces})
