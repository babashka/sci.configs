(ns sci.configs.cjohansen.portfolio.data
  (:require [portfolio.data]
            [sci.core :as sci]))

(def pdns (sci/create-ns 'portfolio.data nil))

(def portfolio-data-namespace 
  {'register-scene! (sci/copy-var portfolio.data/register-scene! pdns)})
