(ns scratch
  (:require [sci.core :as sci]
            [sci.configs.hoplon.javelin]
            [sci.ctx-store]))

(def ctx (sci/init {:namespaces {'javelin.core sci.configs.hoplon.javelin/javelin-core-ns} :classes {'js js/globalThis :allow :all}}))
(sci.ctx-store/reset-ctx! ctx) nil
(sci/eval-string* ctx "(require '[javelin.core :as j])")
(sci/eval-string* ctx "(js/console.log \"test\")")
(prn (sci/eval-string* ctx "(let [a (j/cell 0) b (j/cell= (inc a)) c (j/cell= (js/console.log \"yoooo\" b))] (swap! a inc))"))
