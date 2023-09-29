(ns sci.configs.fulcro.mutations
  (:require [sci.configs.fulcro.fulcro-sci-helpers :as ana]
            [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [sci.core :as sci]
            [com.fulcrologic.fulcro.algorithms.lookup :as ah]
            com.fulcrologic.fulcro.mutations))

(defn ^:sci/macro declare-mutation [_&form _&env name target-symbol]
  `(def ~name (m/->Mutation '~target-symbol)))

(s/def ::handler (s/cat
                  :handler-name symbol?
                  :handler-args (fn [a] (and (vector? a) (= 1 (count a))))
                  :handler-body (s/+ (constantly true))))

(s/def ::mutation-args (s/cat
                        :sym symbol?
                        :doc (s/? string?)
                        :arglist (fn [a] (and (vector? a) (= 1 (count a))))
                        :sections (s/* (s/or :handler ::handler))))

(defn ^:sci/macro defmutation [_&form macro-env args]
  ;; Body of defmutation*
  (let [conform!       (fn [element spec value]
                         (when-not (s/valid? spec value)
                           (throw (ana/error macro-env (str "Syntax error in " element ": " (s/explain-str spec value)))))
                         (s/conform spec value))
        {:keys [sym doc arglist sections]} (conform! "defmutation" ::mutation-args args)
        fqsym          (if (namespace sym)
                         sym
                         (symbol (str (deref sci.core/ns)) #_(name (ns-name *ns*)) (name sym)))
        handlers       (reduce (fn [acc [_ {:keys [handler-name handler-args handler-body]}]]
                                 (let [action? (str/ends-with? (str handler-name) "action")]
                                   (into acc
                                         (if action?
                                           [(keyword (name handler-name)) `(fn ~handler-name ~handler-args
                                                                             (binding [com.fulcrologic.fulcro.raw.components/*after-render* true]
                                                                               ~@handler-body)
                                                                             nil)]
                                           [(keyword (name handler-name)) `(fn ~handler-name ~handler-args ~@handler-body)]))))
                               []
                               sections)
        ks             (into #{} (filter keyword?) handlers)
        result-action? (contains? ks :result-action)
        env-symbol     'fulcro-mutation-env-symbol
        method-map     (if result-action?
                         `{~(first handlers) ~@(rest handlers)}
                         `{~(first handlers) ~@(rest handlers)
                           :result-action    (fn [~'env]
                                               (binding [com.fulcrologic.fulcro.raw.components/*after-render* true]
                                                 (when-let [~'default-action (ah/app-algorithm (:app ~'env) :default-result-action!)]
                                                   (~'default-action ~'env))))})
        doc            (or doc "")
        multimethod    `(defmethod com.fulcrologic.fulcro.mutations/mutate '~fqsym [~env-symbol]
                          (let [~(first arglist) (-> ~env-symbol :ast :params)]
                            ~method-map))]
    (if (= fqsym sym)
      multimethod
      `(do
         (def ~(with-meta sym {:doc doc}) (com.fulcrologic.fulcro.mutations/->Mutation '~fqsym))
         ~multimethod))))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.mutations))
(def ns-def (assoc (sci/copy-ns com.fulcrologic.fulcro.mutations sci-ns
                                {:exclude [declare-mutation defmutation]})
                   'declare-mutation (sci/copy-var declare-mutation sci-ns)
                   'defmutation (sci/copy-var defmutation sci-ns)))

(def namespaces {'com.fulcrologic.fulcro.mutations ns-def})