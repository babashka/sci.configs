(ns sci.configs.re-frame.re-frame
  (:require
   [re-frame.core :as rf]
   [sci.core :as sci]))

(def rfns (sci/create-ns 're-frame.core nil))

(def re-frame-namespace
  {'reg-event-db (sci/copy-var rf/reg-event-db rfns)
   'reg-event-fx (sci/copy-var rf/reg-event-fx rfns)
   'reg-sub (sci/copy-var rf/reg-sub rfns)
   'dispatch-sync (sci/copy-var rf/dispatch-sync rfns)
   'dispatch (sci/copy-var rf/dispatch rfns )
   'subscribe (sci/copy-var rf/subscribe rfns)
   'reg-fx (sci/copy-var rf/reg-fx rfns)})

(def namespaces
  {'re-frame.core re-frame-namespace})

(def config
  {:namespaces namespaces})
