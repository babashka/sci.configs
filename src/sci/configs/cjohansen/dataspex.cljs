(ns sci.configs.cjohansen.dataspex
  (:require [dataspex.core]
            [sci.core :as sci]))

(def dcns (sci/create-ns 'dataspex.core nil))

(def dataspex-core-namespace (sci/copy-ns dataspex.core
                                          dcns
                                          {:exclude [persist! store]}))

(def namespaces {'dataspex.core dataspex-core-namespace})

(def config {:namespaces namespaces})
