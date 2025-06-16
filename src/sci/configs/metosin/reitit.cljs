(ns sci.configs.metosin.reitit
  (:require [reitit.frontend]
            [reitit.frontend.easy]
            [sci.core :as sci]))

(def frontend-namespace
  (sci/copy-ns reitit.frontend (sci/create-ns 'reitit.frontend nil)))
  
(def frontend-easy-namespace
  (sci/copy-ns reitit.frontend.easy (sci/create-ns 'reitit.frontend.easy nil)))
  
(def namespaces {'reitit.frontend frontend-namespace
                 'reitit.frontend.easy frontend-easy-namespace})

(def config {:namespaces namespaces})

