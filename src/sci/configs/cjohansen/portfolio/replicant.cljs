(ns sci.configs.cjohansen.portfolio.replicant
  (:require [sci.configs.cjohansen.portfolio.core :refer [portfolio-core-namespace] :as portfolio]
            [sci.configs.cjohansen.portfolio.data :refer [portfolio-data-namespace]]
            [sci.configs.cjohansen.portfolio.ui :refer [portfolio-ui-namespace]]
            [portfolio.replicant]
            [sci.core :as sci]))

(def prns (sci/create-ns 'portfolio.replicant nil))

(defn ^:sci/macro defscene
  "Execute body with the pretty print dispatch function bound to function."
  [_&form &env id & opts]
  `(portfolio.data/register-scene!
      (portfolio.replicant/create-scene
       ~(portfolio/get-options-map id (:line &env) opts))))
       
#_(defn ^:sci/macro configure-scenes [_ _ & opts]
  `(portfolio.data/register-collection!
      ~@(portfolio/get-collection-options opts)))

(def portfolio-replicant-namespace
  {'create-scene (sci/copy-var portfolio.replicant/create-scene prns)
   'defscene (sci/copy-var defscene prns)})

(def namespaces {'portfolio.core portfolio-core-namespace
                 'portfolio.data portfolio-data-namespace
                 'portfolio.replicant portfolio-replicant-namespace
                 'portfolio.ui portfolio-ui-namespace})

(def config {:namespaces namespaces})