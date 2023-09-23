(ns sci.configs.fulcro.raw.component
  (:require
    [sci.core :as sci]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [taoensso.timbre :as log]))

(defn ^:sci/macro defnc
  ([_&form _&env sym query] (defnc _&form _&env sym query {}))
  ([_&form _&env sym query options]
   (let [nspc (some-> sci.core/ns deref str)
         fqkw (keyword (str nspc) (name sym))
         ]
     `(let [o#     (dissoc (merge ~options {:componentName ~fqkw}) :ident :query)
            ident# (:ident o#)
            ident# (cond
                     (= :constant ident#) (fn [~'_ ~'_] [:Constant/id ~fqkw])
                     (keyword? ident#) (fn [~'_ props#] [ident# (get props# ident#)])
                     (or (nil? ident#) (fn? ident#)) ident#
                     :else (do
                             (log/error "corrupt ident on component " ~fqkw)
                             nil))
            o#     (cond-> o#
                           ident# (assoc :ident ident#))]
        (def ~sym (rc/nc ~query o#))))))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.raw.components))
(def ns-def (assoc (sci/copy-ns com.fulcrologic.fulcro.raw.components sci-ns {:exclude [defnc]})
          'defnc (sci/copy-var defnc sci-ns)))

(def namespaces {'com.fulcrologic.fulcro.raw.components ns-def})
