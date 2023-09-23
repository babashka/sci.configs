(ns sci.configs.fulcro.ui-state-machines
  (:require [sci.core :as sci]
            [com.fulcrologic.fulcro.ui-state-machines :as uism]))

(defn ^:sci/macro defstatemachine [_&form _&env name body]
  (let [nmspc       (str (deref sci.core/ns) #_(ns-name *ns*))
        storage-sym (symbol nmspc (str name))]
    `(do
       (def ~name (assoc ~body ::uism/state-machine-id '~storage-sym))
       (uism/register-state-machine! '~storage-sym ~body))))
  
(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.ui-state-machines))
(def ns-def (assoc (sci/copy-ns com.fulcrologic.fulcro.ui-state-machines sci-ns
                                {:exclude [defstatemachine]})
                   'defstatemachine (sci/copy-var defstatemachine sci-ns)))
  
(def namespaces {'com.fulcrologic.fulcro.ui-state-machines ns-def})