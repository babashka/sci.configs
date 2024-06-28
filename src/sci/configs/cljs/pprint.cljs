(ns sci.configs.cljs.pprint
  (:require [cljs.pprint :as pp]
            [sci.core :as sci]))

(def pns (sci/create-ns 'cljs.pprint nil))

(defn pprint [& args]
  (binding [*print-fn* @sci/print-fn
            *print-newline* @sci/print-newline]
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
   'print-table (sci/copy-var print-table pns)
   'cl-format (sci/copy-var cl-format pns)})

(def namespaces {'cljs.pprint cljs-pprint-namespace})

(def config {:namespaces namespaces})
