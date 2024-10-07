(ns sci.configs.cljs.pprint
  (:require [cljs.pprint :as pp]
            [sci.core :as sci]))

(def pns (sci/create-ns 'cljs.pprint nil))

(defn ^:sci/macro with-pprint-dispatch
  "Execute body with the pretty print dispatch function bound to function."
  [_ _ function & body]
  `(cljs.core/binding [cljs.pprint/*print-pprint-dispatch* ~function]
     ~@body))

(def print-pprint-dispatch (sci/new-dynamic-var '*print-pprint-dispatch*
                                                cljs.pprint/*print-pprint-dispatch* {:ns pns}))

(defn pprint [& args]
  (binding [*print-fn* @sci/print-fn
            *print-newline* @sci/print-newline
            pp/*print-pprint-dispatch* @print-pprint-dispatch]
    (apply pp/pprint args)))

(defn print-table [& args]
  (binding [*print-fn* @sci/print-fn
            *print-newline* @sci/print-newline]
    (apply pp/print-table args)))

(defn cl-format [& args]
  (binding [*print-fn* @sci/print-fn
            *print-newline* @sci/print-newline]
    (apply pp/cl-format args)))

(def cljs-pprint-namespace
  {'pprint (sci/copy-var pprint pns)
   '*print-pprint-dispatch* print-pprint-dispatch
   'print-table (sci/copy-var print-table pns)
   'cl-format (sci/copy-var cl-format pns)
   'code-dispatch (sci/copy-var pp/code-dispatch pns)
   'with-pprint-dispatch (sci/copy-var with-pprint-dispatch pns)})

(def namespaces {'cljs.pprint cljs-pprint-namespace})

(def config {:namespaces namespaces})
