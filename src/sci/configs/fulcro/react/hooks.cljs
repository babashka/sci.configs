(ns sci.configs.fulcro.react.hooks
  (:require [sci.core :as sci]
            [com.fulcrologic.fulcro.react.hooks :as hooks])
  #_(:import (cljs.tagged_literals JSValue))) ; not avail. in cljs

(defn ^:sci/macro use-effect 
  ([_&form _&env f] `(hooks/useEffect ~f))
  ([_&form _&env f dependencies]
   (if true #_(enc/compiling-cljs?)
       (let [deps (cond
                    (nil? dependencies) nil
                    ; JH: Not sure how to translate this to a sci/macro...
                    ;(instance? JSValue dependencies) dependencies
                    ;:else (JSValue. dependencies)
                    (instance? js/Array dependencies) dependencies
                    (sequential? dependencies) (into-array dependencies)

                    :else dependencies)]
         `(hooks/useEffect ~f ~deps))
       `(hooks/useEffect ~f ~dependencies))))

(defn ^:sci/macro use-lifecycle 
  ([_&form _&env setup] `(hooks/use-lifecycle &form &env ~setup nil))
  ([_&form _&env setup teardown]
   (cond
     (and setup teardown) `(hooks/use-effect (fn [] (~setup) ~teardown) [])
     setup `(hooks/use-effect (fn [] (~setup) ~(when true #_(enc/compiling-cljs?) 'js/undefined)) [])
     teardown `(hooks/use-effect (fn [] ~teardown) []))))
  
(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.react.hooks))
(def ns-def (assoc (sci/copy-ns com.fulcrologic.fulcro.react.hooks sci-ns
                                {:exclude [use-effect use-lifecycle]})
                   'use-effect (sci/copy-var use-effect sci-ns) 
                   'use-lifecycle (sci/copy-var use-lifecycle sci-ns)))
  
(def namespaces {'com.fulcrologic.fulcro.react.hooks ns-def})