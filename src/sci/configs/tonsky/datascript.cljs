(ns sci.configs.tonsky.datascript
  (:require [datascript.core :as d]
            [datascript.db :as db]
            [datascript.storage :as storage]
            [sci.impl.types :as types]
            [sci.core :as sci :refer [copy-var]]))

(def core-ns (sci/create-ns 'datascript.core nil))
(def db-ns   (sci/create-ns 'datascript.db nil))
(def storage-ns   (sci/create-ns 'datascript.storage nil))

(def core-namespace
  {'q (copy-var d/q core-ns)
   'empty-db (copy-var d/empty-db core-ns)
   'db-with (copy-var d/db-with core-ns)
   'filter (copy-var d/filter core-ns)
   'init-db (copy-var d/init-db core-ns)
   'datom (copy-var d/datom core-ns)
   'datoms (copy-var d/datoms core-ns)
   'pull (copy-var d/pull core-ns)
   'pull-many (copy-var d/pull-many core-ns)
   'entity (copy-var d/entity core-ns)
   'tx0 (copy-var d/tx0 core-ns)
   'db (copy-var d/db core-ns)
   'squuid (copy-var d/squuid core-ns)
   'with (copy-var d/with core-ns)
   'touch (copy-var d/touch core-ns)
   'index-range (copy-var d/index-range core-ns)
   'listen! (copy-var d/listen! core-ns)
   'conn-from-db (copy-var d/conn-from-db core-ns)
   'conn-from-datoms (copy-var d/conn-from-datoms core-ns)
   'transact! (copy-var d/transact! core-ns)
   'create-conn (copy-var d/create-conn core-ns)
   'reset-conn! (copy-var d/reset-conn! core-ns)
   'restore-conn (copy-var d/restore-conn core-ns)
   'settings (copy-var d/settings core-ns)
   'from-serializable (copy-var d/from-serializable core-ns)
   'serializable (copy-var d/serializable core-ns)})

(def db-namespace
  {'db-from-reader    (copy-var db/db-from-reader db-ns)
   'datom-from-reader (copy-var db/datom-from-reader db-ns)
   'datom-added       (copy-var db/datom-added db-ns)
   'datom-tx          (copy-var db/datom-tx db-ns)
   'datom             (copy-var db/datom db-ns)
   'DB                (copy-var db/DB db-ns)
   'Datom             (copy-var db/Datom db-ns)})

;;;; IDeref

(defmulti -store types/type-impl)
(defmulti -restore types/type-impl)

(defmethod -store :sci.impl.protocols/reified [store addr+data-seq]
  (let [methods (types/getMethods store)]
    ((get methods -store) store addr+data-seq)))

(def store-default
  (defmethod -store :default [store addr+data-seq]
    (storage/-store store addr+data-seq)))

(defmethod -restore :sci.impl.protocols/reified [store addr]
  (let [methods (types/getMethods store)]
    ((get methods -restore) store addr)))

(def restore-default
  (defmethod -restore :default [store addr+data-seq]
    (storage/-restore store addr+data-seq)))

(def IStorage-protocol
  (sci/new-var
   'datascript.storage.IStorage
   {:protocol datascript.storage.IStorage
    :methods #{-store -restore}
    :ns storage-ns}))

;;;; end IDeref


(def storage-namespace (assoc (sci/copy-ns datascript.storage storage-ns)
                              '-store (sci/copy-var -store storage-ns)
                              '-restore (sci/copy-var -restore storage-ns)
                              'IStorage IStorage-protocol))

(def namespaces {'datascript.core core-namespace
                 'datascript.db   db-namespace
                 'datascript.storage storage-namespace})

(def config {:namespaces namespaces})
