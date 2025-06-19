(ns sci.configs.cjohansen.replicant
  (:require [replicant.alias :as ra]
            [replicant.dom :as rd]
            [replicant.string :as rs]
            [sci.core :as sci]
            [replicant.assert :as assert]))

(def rdns (sci/create-ns 'replicant.dom nil))

(def replicant-dom-namespace
  {'render (sci/copy-var rd/render rdns)
   'unmount (sci/copy-var rd/unmount rdns)
   'set-dispatch! (sci/copy-var rd/set-dispatch! rdns)})

(def rsns (sci/create-ns 'replicant.string nil))

(def replicant-string-namespace
  {'create-renderer (sci/copy-var rs/create-renderer rsns)
   'render (sci/copy-var rs/render rsns)})

(def rans (sci/create-ns 'replicant.alias nil))

(defn ^:sci/macro aliasfn
  "Define a function to use as an alias function. Creates a function that wraps
  returned hiccup with debugging meta data when Replicant asserts are
  enabled (e.g. during development). When asserts are not enabled (default for
  production builds), creates a regular function with no added overhead.
 
  `aliasfn` is most commonly used through `defalias`"
  [_ _ alias & forms]
  (let [[_docstring [attr-map & body]]
        (if (string? (first forms))
          [(first forms) (next forms)]
          ["" forms])
        n-args (count attr-map)
        attr-map (cond
                   (= 0 n-args)
                   '[_ _]
 
                   (= 1 n-args)
                   (conj attr-map '_)
 
                   :else
                   attr-map)]
    (if (assert/assert?)
      `(with-meta
         (fn [& args#]
           (let [~attr-map args#
                 res# (do ~@body)]
             (cond-> res#
               (vector? res#)
               (with-meta
                 {:replicant/context
                  {:alias ~alias
                   :data (first args#)}}))))
         {:replicant/alias ~alias})
      `(with-meta (fn ~attr-map ~@body) {:replicant/alias ~alias}))))

(defn ^:sci/macro defalias
  "Creates a function to render `alias` (a namespaced keyword), and registers
  it in the global registry. See `aliasfn` for details about the created function.
  The global registry is available through `replicant.alias/get-registered-aliases`."
  [_ _ alias & forms]
  (let [alias-kw (keyword (deref sci/ns) #_(str *ns*) (name alias))
        alias-f `(replicant.alias/aliasfn ~alias-kw ~@forms)]
    `(let [f# ~alias-f
           alias# ~alias-kw]
       (replicant.alias/register! alias# f#)
       (def ~alias alias#))))

(def replicant-alias-namespace
  {'register! (sci/copy-var ra/register! rsns)
   'aliasfn (sci/copy-var aliasfn rsns)
   'defalias (sci/copy-var defalias rsns)})
  
(def replicant-assert-namespace
  (sci/copy-ns replicant.assert (sci/create-ns 'replicant.assert nil)))

(def namespaces {'replicant.dom replicant-dom-namespace
                 'replicant.string replicant-string-namespace
                 'replicant.alias replicant-alias-namespace
                 'replicant.assert replicant-assert-namespace})

(def config {:namespaces namespaces})
