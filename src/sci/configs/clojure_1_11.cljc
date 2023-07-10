(ns sci.configs.clojure-1-11
  (:require [sci.core :as sci]
            [sci.impl.utils :refer [clojure-core-ns]]
            [clojure.core :as c]))

(def ^:private clojure-core-namespace-extras-1-11
  {'abs (sci/copy-var c/abs clojure-core-ns)
   'NaN? (sci/copy-var c/NaN? clojure-core-ns)
   'infinite? (sci/copy-var c/infinite? clojure-core-ns)
   'parse-double (sci/copy-var c/parse-double clojure-core-ns)
   'parse-long (sci/copy-var c/parse-long clojure-core-ns)
   'parse-boolean (sci/copy-var c/parse-boolean clojure-core-ns)
   'parse-uuid (sci/copy-var c/parse-uuid clojure-core-ns)
   'random-uuid (sci/copy-var c/random-uuid clojure-core-ns)
   'update-keys (sci/copy-var c/update-keys clojure-core-ns)
   'update-vals (sci/copy-var c/update-vals clojure-core-ns)
   'iteration (sci/copy-var c/iteration clojure-core-ns)})


(def namespaces
  {'clojure.core clojure-core-namespace-extras-1-11})