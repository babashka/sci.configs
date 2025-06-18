(ns sci.configs.cjohansen.portfolio.ui
  (:require [portfolio.ui :as rd]
            [sci.core :as sci]))

(def rdns (sci/create-ns 'portfolio.ui nil))

(def portfolio-ui-namespace
  {'start! (sci/copy-var rd/start! rdns)})
