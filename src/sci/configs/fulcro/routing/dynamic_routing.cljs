(ns sci.configs.fulcro.routing.dynamic-routing
  (:require [sci.configs.fulcro.fulcro-sci-helpers :as ana]
            [com.fulcrologic.fulcro.raw.components :as rc]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [com.fulcrologic.fulcro.ui-state-machines :as uism]
            [sci.core :as sci]))

(defn compile-error [env form message]
  (throw (ana/error (merge env (some-> form meta)) message)))

(defn ^:sci/macro defrouter [_&form env router-sym arglist options & body]
  (let [router-ns (str (deref sci.core/ns) #_(ns-name *ns*))]
    ;; copied body of defrouter*
    (when-not (and (vector? arglist) (= 2 (count arglist)))
      (compile-error env options "defrouter argument list must have an entry for this and props."))
    (when-not (map? options)
      (compile-error env options "defrouter requires a literal map of options."))
    #_(when-not (s/valid? ::defrouter-options options) ; JH - disabled spec check
      (compile-error env options (str "defrouter options are invalid: " (s/explain-str ::defrouter-options options))))
     (let [{:keys [router-targets]} options
           _                      (when (empty? router-targets)
                                    (compile-error env options "defrouter requires a vector of :router-targets with at least one target"))
           id                     (keyword router-ns (name router-sym))
           getq                   (fn [s] `(or (rc/get-query ~s)
                                               (throw (ex-info (str "Route target has no query! "
                                                                    (rc/component-name ~s)) {}))))
           query                  (into [::dr/id
                                         [::uism/asm-id id]
                                         ::dr/dynamic-router-targets
                                         {::dr/current-route (getq (first router-targets))}]
                                        (map-indexed
                                         (fn [idx s]
                                           (when (nil? s)
                                             (compile-error env options "defrouter :target contains nil!"))
                                           {(keyword (str "alt" idx)) (getq s)})
                                         (rest router-targets)))
           initial-state-map      (into {::dr/id            id
                                         ::dr/current-route `(rc/get-initial-state ~(first router-targets) ~'params)}
                                        (map-indexed
                                         (fn [idx s] [(keyword (str "alt" idx)) `(rc/get-initial-state ~s {})])
                                         (rest router-targets)))
           ident-method           (apply list `(fn [] [::dr/id ~id]))
           initial-state-lambda   (apply list `(fn [~'params] ~initial-state-map))
           states-to-render-route (if (seq body)
                                    #{:routed :deferred}
                                    `(constantly true))
           always-render-body?    (and (map? options) (:always-render-body? options))
           render-cases           (if always-render-body?
                                    (apply list `(let [~'class (dr/current-route-class ~'this)]
                                                   (let [~(first arglist) ~'this
                                                         ~(second arglist) {:pending-path-segment ~'pending-path-segment
                                                                            :route-props          ~'current-route
                                                                            :route-factory        (when ~'class (comp/factory ~'class))
                                                                            :current-state        ~'current-state
                                                                            :router-state         (get-in ~'props [[::uism/asm-id ~id] ::uism/local-storage])}]
                                                     ~@body)))
                                    (apply list `(let [~'class (dr/current-route-class ~'this)]
                                                   (if (~states-to-render-route ~'current-state)
                                                     (when ~'class
                                                       (let [~'factory (comp/factory ~'class)]
                                                         (~'factory (rc/computed ~'current-route (rc/get-computed ~'this)))))
                                                     (let [~(first arglist) ~'this
                                                           ~(second arglist) {:pending-path-segment ~'pending-path-segment
                                                                              :route-props          ~'current-route
                                                                              :route-factory        (when ~'class (comp/factory ~'class))
                                                                              :current-state        ~'current-state}]
                                                       ~@body)))))
           options                (merge
                                   `{:componentDidMount (fn [this#] (dr/validate-route-targets this#))}
                                   options
                                   `{:query                   ~query
                                     :ident                   ~ident-method
                                     :use-hooks?              false
                                     :initial-state           ~initial-state-lambda
                                     :preserve-dynamic-query? true})]
       `(comp/defsc ~router-sym [~'this {::dr/keys [~'id ~'current-route] :as ~'props}]
          ~options
          (let [~'current-state (uism/get-active-state ~'this ~id)
                ~'state-map (comp/component->state-map ~'this)
                ~'sm-env (uism/state-machine-env ~'state-map nil ~id :fake {})
                ~'pending-path-segment (when (uism/asm-active? ~'this ~id) (uism/retrieve ~'sm-env :pending-path-segment))]
            ~render-cases)))))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.routing.dynamic-routing))
(def ns-def (assoc (sci/copy-ns com.fulcrologic.fulcro.routing.dynamic-routing sci-ns
                                {:exclude [defrouter]})
                   'defrouter (sci/copy-var defrouter sci-ns) ))

(def namespaces {'com.fulcrologic.fulcro.routing.dynamic-routing ns-def})