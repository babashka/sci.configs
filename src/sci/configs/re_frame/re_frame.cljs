(ns sci.configs.re-frame.re-frame
  (:require
   [re-frame.core :as rf]
   [sci.core :as sci]))

(def rfns (sci/create-ns 're-frame.core nil))

(def re-frame-namespace
  {'dispatch (sci/copy-var rf/dispatch rfns)
   'dispatch-sync (sci/copy-var rf/dispatch-sync rfns)
   'reg-event-db (sci/copy-var rf/reg-event-db rfns)
   'reg-event-fx (sci/copy-var rf/reg-event-fx rfns)
   'reg-event-ctx (sci/copy-var rf/reg-event-ctx rfns)
   'clear-event (sci/copy-var rf/clear-event rfns)
   'reg-sub (sci/copy-var rf/reg-sub rfns)
   'subscribe (sci/copy-var rf/subscribe rfns)
   'clear-sub (sci/copy-var rf/clear-sub rfns)
   'reg-sub-raw (sci/copy-var rf/reg-sub-raw rfns)
   'clear-subscription-cache! (sci/copy-var rf/clear-subscription-cache! rfns)
   'reg-fx (sci/copy-var rf/reg-fx rfns)
   'clear-fx (sci/copy-var rf/clear-fx rfns)
   'reg-cofx (sci/copy-var rf/reg-cofx rfns)
   'inject-cofx (sci/copy-var rf/inject-cofx rfns)
   'clear-cofx (sci/copy-var rf/clear-cofx rfns)
   'debug (sci/copy-var rf/debug rfns)
   'path (sci/copy-var rf/path rfns)
   'enrich (sci/copy-var rf/enrich rfns)
   'unwrap (sci/copy-var rf/unwrap rfns)
   'trim-v (sci/copy-var rf/trim-v rfns)
   'after (sci/copy-var rf/after rfns)
   'on-changes (sci/copy-var rf/on-changes rfns)
   'reg-global-interceptor (sci/copy-var rf/reg-global-interceptor rfns)
   'clear-global-interceptor (sci/copy-var rf/clear-global-interceptor rfns)
   '->interceptor (sci/copy-var rf/->interceptor rfns)
   'get-coeffect (sci/copy-var rf/get-coeffect rfns)
   'assoc-coeffect (sci/copy-var rf/assoc-coeffect rfns)
   'get-effect (sci/copy-var rf/get-effect rfns)
   'assoc-effect (sci/copy-var rf/assoc-effect rfns)
   'enqueue (sci/copy-var rf/enqueue rfns)
   'set-loggers! (sci/copy-var rf/set-loggers! rfns)
   'console (sci/copy-var rf/console rfns)
   'make-restore-fn (sci/copy-var rf/make-restore-fn rfns)
   'purge-event-queue (sci/copy-var rf/purge-event-queue rfns)
   'add-post-event-callback (sci/copy-var rf/add-post-event-callback rfns)
   'remove-post-event-callback (sci/copy-var rf/remove-post-event-callback rfns)})

(def namespaces
  {'re-frame.core re-frame-namespace})

(def config
  {:namespaces namespaces})
