(ns sci.configs.fulcro.component
  (:require
   [cljs.spec.alpha :as s]
   [clojure.set :as set]
   [clojure.walk :refer [prewalk]]
   [edn-query-language.core :as eql]
   [sci.core :as sci]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.algorithms.do-not-use :as util]
   [sci.configs.fulcro.fulcro-sci-helpers :as ana]))

(def cljs? (constantly true)) ; was `(:ns &env)` but sci's &env lacks :ns

(defn with-parent-context-fn
  [parent body-fn]
  (let [app    (or comp/*app* (comp/any->app parent))
        s      (comp/shared app)
        p      (or comp/*parent* parent)]
    (binding [comp/*app*    app
              comp/*shared* s
              comp/*parent* p]
      (body-fn))))

(defn ^:sci/macro with-parent-context
  [_&form &env outer-parent & body]
  (if-not (cljs? &env) ; was (:ns &env) 
    `(do ~@body)
    ;; Re-written to move the core into a separate fn, so that `binding` happens
    ;; there and not in code that SCI needs to evaluate, which has its complexities
    `(with-parent-context-fn ~outer-parent (fn [] ~@body))))

(defn- is-link?
  "Returns true if the given query element is a link query like [:x '_]."
  [query-element] (and (vector? query-element)
                       (keyword? (first query-element))
                       ; need the double-quote because when in a macro we'll get the literal quote. ; TODO is this true for SCI ?!
                       (#{''_ '_} (second query-element))))

(defn- children-by-prop ; TODO clj-only in Fulcro proper, but could be for cljs too?!
  "Part of Defsc macro implementation. Calculates a map from join key to class (symbol)."
  [query]
  (into {}
        (keep #(if (and (map? %) (or (is-link? (ffirst %)) (keyword? (ffirst %))))
                 (let [k   (if (vector? (ffirst %))
                             (first (ffirst %))
                             (ffirst %))
                       cls (-> % first second second)]
                   [k cls])
                 nil) query)))

(defn- replace-and-validate-fn ; TODO clj-only in Fulcro proper, but could be for cljs too?!
  "Replace the first sym in a list (the function name) with the given symbol.

  env - the macro &env
  sym - The symbol that the lambda should have
  external-args - A sequence of arguments that the user should not include, but that you want to be inserted in the external-args by this function.
  user-arity - The number of external-args the user should supply (resulting user-arity is (count external-args) + user-arity).
  fn-form - The form to rewrite
  sym - The symbol to report in the error message (in case the rewrite uses a different target that the user knows)."
  ([env sym external-args user-arity fn-form] (replace-and-validate-fn env sym external-args user-arity fn-form sym))
  ([env sym external-args user-arity fn-form user-known-sym]
   (when-not (<= user-arity (count (second fn-form)))
     (throw (ana/error (merge env (meta fn-form)) (str "Invalid arity for " user-known-sym ". Expected " user-arity " or more."))))
   (let [user-args    (second fn-form)
         updated-args (into (vec (or external-args [])) user-args)
         body-forms   (drop 2 fn-form)]
     (->> body-forms
          (cons updated-args)
          (cons sym)
          (cons 'fn)))))

(defn- build-ident ; TODO clj-only in Fulcro proper, but could be for cljs too?!
  "Builds the ident form. If ident is a vector, then it generates the function and validates that the ID is
  in the query. Otherwise, if ident is of the form (ident [this props] ...) it simply generates the correct
  entry in defsc without error checking."
  [env thissym propsarg {:keys [method template keyword]} is-legal-key?]
  (cond
    keyword (if (is-legal-key? keyword)
              `(~'fn ~'ident* [~'_ ~'props] [~keyword (~keyword ~'props)])
              (throw (ana/error (merge env (meta template)) (str "The table/id " keyword " of :ident does not appear in your :query"))))
    method (replace-and-validate-fn env 'ident* [thissym propsarg] 0 method)
    template (let [table   (first template)
                   id-prop (or (second template) :db/id)]
               (cond
                 (nil? table) (throw (ana/error (merge env (meta template)) "TABLE part of ident template was nil" {}))
                 (not (is-legal-key? id-prop)) (throw (ana/error (merge env (meta template)) (str "The ID property " id-prop " of :ident does not appear in your :query")))
                 :otherwise `(~'fn ~'ident* [~'this ~'props] [~table (~id-prop ~'props)])))))

(defn- build-render [classsym thissym propsym compsym extended-args-sym body] ; TODO clj-only in Fulcro proper, but could be for cljs too?!
   (let [computed-bindings (when compsym `[~compsym (com.fulcrologic.fulcro.components/get-computed ~thissym)])
         extended-bindings (when extended-args-sym `[~extended-args-sym (com.fulcrologic.fulcro.components/get-extra-props ~thissym)])
         render-fn (symbol (str "render-" (name classsym)))]
     `(~'fn ~render-fn [~thissym]
        (com.fulcrologic.fulcro.components/wrapped-render ~thissym
                                                          (fn []
                                                            (let [~propsym (com.fulcrologic.fulcro.components/props ~thissym)
                                                                  ~@computed-bindings
                                                                  ~@extended-bindings]
                                                              ~@body))))))

(defn- build-hooks-render [classsym thissym propsym compsym extended-args-sym body] ; TODO clj-only in Fulcro proper, but could be for cljs too?!
   (let [computed-bindings (when compsym `[~compsym (com.fulcrologic.fulcro.components/get-computed ~thissym)])
         extended-bindings (when extended-args-sym `[~extended-args-sym (com.fulcrologic.fulcro.components/get-extra-props ~thissym)])
         render-fn (symbol (str "render-" (name classsym)))]
     `(~'fn ~render-fn [~thissym ~propsym]
        (com.fulcrologic.fulcro.components/wrapped-render ~thissym
                                                          (fn []
                                                            (binding [comp/*app* (or comp/*app* (comp/isoget-in ~thissym ["props" "fulcro$app"]))
                                                                      comp/*shared* (comp/shared (or comp/*app* (comp/isoget-in ~thissym ["props" "fulcro$app"])))
                                                                      comp/*parent* ~thissym]
                                                              (let [~@computed-bindings
                                                                    ~@extended-bindings]
                                                                ~@body)))))))

(defn- build-and-validate-initial-state-map [env sym initial-state legal-keys children-by-query-key]
   (let [env (merge env (meta initial-state))
         join-keys (set (keys children-by-query-key))
         init-keys (set (keys initial-state))
         illegal-keys (if (set? legal-keys) (set/difference init-keys legal-keys) #{})
         is-child? (fn [k] (contains? join-keys k))
         param-expr (fn [v]
                      (if-let [kw (and (keyword? v) (= "param" (namespace v))
                                       (keyword (name v)))]
                        `(~kw ~'params)
                        v))
         parameterized (fn [init-map] (into {} (map (fn [[k v]] (if-let [expr (param-expr v)] [k expr] [k v])) init-map)))
         child-state (fn [k]
                       (let [state-params (get initial-state k)
                             to-one? (map? state-params)
                             to-many? (and (vector? state-params) (every? map? state-params))
                             code? (list? state-params)
                             from-parameter? (and (keyword? state-params) (= "param" (namespace state-params)))
                             child-class (get children-by-query-key k)]
                         (when code?
                           (throw (ana/error env (str "defsc " sym ": Illegal parameters to :initial-state " state-params ". Use a lambda if you want to write code for initial state. Template mode for initial state requires simple maps (or vectors of maps) as parameters to children. See Developer's Guide."))))
                         (cond
                           (not (or from-parameter? to-many? to-one?)) (throw (ana/error env (str "Initial value for a child (" k ") must be a map or vector of maps!")))
                           to-one? `(com.fulcrologic.fulcro.components/get-initial-state ~child-class ~(parameterized state-params))
                           to-many? (mapv (fn [params]
                                            `(com.fulcrologic.fulcro.components/get-initial-state ~child-class ~(parameterized params)))
                                          state-params)
                           from-parameter? `(com.fulcrologic.fulcro.components/get-initial-state ~child-class ~(param-expr state-params))
                           :otherwise nil)))
         kv-pairs (map (fn [k]
                         [k (if (is-child? k)
                              (child-state k)
                              (param-expr (get initial-state k)))]) init-keys)
         state-map (into {} kv-pairs)]
     (when (seq illegal-keys)
       (throw (ana/error env (str "Initial state includes keys " illegal-keys ", but they are not in your query."))))
     `(~'fn ~'build-initial-state* [~'params] (com.fulcrologic.fulcro.components/make-state-map ~initial-state ~children-by-query-key ~'params))))

(defn- build-raw-initial-state ; TODO clj-only in Fulcro proper, but could be for cljs too?!
   "Given an initial state form that is a list (function-form), simple copy it into the form needed by defsc."
   [env method]
   (replace-and-validate-fn env 'build-raw-initial-state* [] 1 method))

(defn- build-initial-state [env sym {:keys [template method]} legal-keys query-template-or-method] ; TODO clj-only in Fulcro proper, but could be for cljs too?!
  (when (and template (contains? query-template-or-method :method))
    (throw (ana/error (merge env (meta template)) (str "When query is a method, initial state MUST be as well."))))
  (cond
    method (build-raw-initial-state env method)
    template (let [query (:template query-template-or-method)
                   children (or (children-by-prop query) {})]
               (build-and-validate-initial-state-map env sym template legal-keys children))))

(defn -legal-keys ; TODO clj-only in Fulcro proper, but could be for cljs too?!
  "PRIVATE. Find the legal keys in a query. NOTE: This is at compile time, so the get-query calls are still embedded (thus cannot
  use the AST)"
  [query]
  (letfn [(keeper [ele]
            (cond
              (list? ele) (recur (first ele))
              (keyword? ele) ele
              (is-link? ele) (first ele)
              (and (map? ele) (keyword? (ffirst ele))) (ffirst ele)
              (and (map? ele) (is-link? (ffirst ele))) (first (ffirst ele))
              :else nil))]
    (set (keep keeper query))))

(defn- component-query [query-part] ; TODO clj-only in Fulcro proper, but could be for cljs too?!
  (and (list? query-part)
       (symbol? (first query-part))
       (= "get-query" (name (first query-part)))
       query-part))

(defn- compile-time-query->checkable ; TODO clj-only in Fulcro proper, but could be for cljs too?! (only Throwable <> :default)
   "Try to simplify the compile-time query (as seen by the macro)
   to something that EQL can check (`(get-query ..)` => a made-up vector).
   Returns nil if this is not possible."
   [query]
   (try
     (prewalk
       (fn [form]
         (cond
           (component-query form)
           [(keyword (str "subquery-of-" (some-> form second name)))]

           ;; Replace idents with idents that contain only keywords, so syms don't trip us up
           (and (vector? form) (= 2 (count form)))
           (mapv #(if (symbol? %) :placeholder %) form)

           (symbol? form)
           (throw (ex-info "Cannot proceed, the query contains a symbol" {:sym form}))

           :else
           form))
       query)
     (catch :default _ ; Changed - was Throwable
       nil)))

(defn- check-query-looks-valid [err-env comp-class compile-time-query] ; TODO clj-only in Fulcro proper, but could be for cljs too?!
   (let [checkable-query (compile-time-query->checkable compile-time-query)]
     (when (false? (some->> checkable-query (s/valid? ::eql/query)))
       (let [{:clojure.spec.alpha/keys [problems]} (s/explain-data ::eql/query checkable-query)
             {:keys [in]} (first problems)]
         (when (vector? in)
           (throw (ana/error err-env (str "The element '" (get-in compile-time-query in) "' of the query of " comp-class " is not valid EQL"))))))))


(defn- build-query-forms ; TODO clj-only in Fulcro proper, but could be for cljs too?!
  "Validate that the property destructuring and query make sense with each other."
  [env class thissym propargs {:keys [template method]}]
  (cond
    template
    (do
      (assert (or (symbol? propargs) (map? propargs)) "Property args must be a symbol or destructuring expression.")
      (let [to-keyword            (fn [s] (cond
                                            (nil? s) nil
                                            (keyword? s) s
                                            :otherwise (let [nspc (namespace s)
                                                             nm   (name s)]
                                                         (keyword nspc nm))))
            destructured-keywords (when (map? propargs) (util/destructured-keys propargs))
            queried-keywords      (-legal-keys template)
            has-wildcard?         (some #{'*} template)
            to-sym                (fn [k] (symbol (namespace k) (name k)))
            illegal-syms          (mapv to-sym (set/difference destructured-keywords queried-keywords))
            err-env               (merge env (meta template))]
        (when-let [child-query (some component-query template)]
          (throw (ana/error err-env (str "defsc " class ": `get-query` calls in :query can only be inside a join value, i.e. `{:some/key " child-query "}`"))))
        (when (and (not has-wildcard?) (seq illegal-syms))
          (throw (ana/error err-env (str "defsc " class ": " illegal-syms " was destructured in props, but does not appear in the :query!"))))
        `(~'fn ~'query* [~thissym] ~template)))
    method
    (replace-and-validate-fn env 'query* [thissym] 0 method)))

;; Copied b/c they are :clj only in the orig ns
(s/def ::ident (s/or :template (s/and vector? #(= 2 (count %))) :method list? :keyword keyword?))
(s/def ::query (s/or :template vector? :method list?))
(s/def ::initial-state (s/or :template map? :method list?))
(s/def ::options (s/keys :opt-un [::query ::ident ::initial-state]))
(s/def ::args (s/cat
                :sym symbol?
                :doc (s/? string?)
                :arglist (s/and vector? #(<= 2 (count %) 5))
                :options (s/? map?)
                :body (s/* any?)))

(defn defsc*
  [env args]
  (when-not (s/valid? ::args args)
    (throw (ana/error env (str "Invalid arguments. " (-> (s/explain-data ::args args)
                                                         ::s/problems
                                                         first
                                                         :path) " is invalid."))))
  (let [{:keys [sym doc arglist options body]} (s/conform ::args args)
        [thissym propsym computedsym extra-args] arglist
        _ (when (and options (not (s/valid? ::options options)))
            (let [path (-> (s/explain-data ::options options) ::s/problems first :path)
                  message (cond
                            (= path [:query :template]) "The query template only supports vectors as queries. Unions or expression require the lambda form."
                            (= :ident (first path)) "The ident must be a keyword, 2-vector, or lambda of no arguments."
                            :else "Invalid component options. Please check to make\nsure your query, ident, and initial state are correct.")]
              (throw (ana/error env message))))
        {:keys [ident query initial-state]} (s/conform ::options options)
        body (or body ['nil])
        ident-template-or-method (into {} [ident]) ;clojure spec returns a map entry as a vector
        initial-state-template-or-method (into {} [initial-state])
        query-template-or-method (into {} [query])
        validate-query? (and (:template query-template-or-method) (not (some #{'*} (:template query-template-or-method))))
        legal-key-checker (if validate-query?
                            (or (-legal-keys (:template query-template-or-method)) #{})
                            (complement #{}))
        ident-form (build-ident env thissym propsym ident-template-or-method legal-key-checker)
        state-form (build-initial-state env sym initial-state-template-or-method legal-key-checker query-template-or-method)
        query-form (build-query-forms env sym thissym propsym query-template-or-method)
        _ (when validate-query?
            ;; after build-query-forms as it also does some useful checks
            (check-query-looks-valid env sym (:template query-template-or-method)))
        hooks? (and (cljs? env) (:use-hooks? options))
        render-form (if hooks?
                      (build-hooks-render sym thissym propsym computedsym extra-args body)
                      (build-render sym thissym propsym computedsym extra-args body))
        nspc (if (cljs? env) (-> env :ns :name str) (name (ns-name *ns*)))
        fqkw (keyword (str nspc) (name sym))
        options-map (cond-> options
                            state-form (assoc :initial-state state-form)
                            ident-form (assoc :ident ident-form)
                            query-form (assoc :query query-form)
                            hooks? (assoc :componentName fqkw)
                            render-form (assoc :render render-form))]
    (cond
      hooks?
      `(do
         (defonce ~sym
                  (fn [js-props#]
                    (let [render# (:render (comp/component-options ~sym))
                          [this# props#] (comp/use-fulcro js-props# ~sym)]
                      (render# this# props#))))
         (comp/add-hook-options! ~sym ~options-map))

      (cljs? env)
      `(do
         (declare ~sym)
         (let [options# ~options-map]
           (def ~(vary-meta sym assoc :doc doc :jsdoc ["@constructor"]) ; JH: BEWARE `defonce` will prevent changes in :advanced optimiz.
             (comp/react-constructor (get options# :initLocalState)))
           (com.fulcrologic.fulcro.components/configure-component! ~sym ~fqkw options#)))

      :else
      `(do
         (declare ~sym)
         (let [options# ~options-map]
           (def ~(vary-meta sym assoc :doc doc :once true)
             (com.fulcrologic.fulcro.components/configure-component! ~(str sym) ~fqkw options#)))))))

(defn ^:sci/macro defsc [_&form &env & args]
  (try
    ;; Note: In cljs, env would have `:ns` but not so in SCI, yet Fulcro looks at it => add
    (let [ns-name (some->> sci.core/ns deref str)
          fake-ns (when (seq ns-name) {:name ns-name})]
     (defsc* (assoc &env :ns fake-ns) args))
    (catch :default e
      (if (contains? (ex-data e) :tag)
        (throw e)
        (throw (ex-info "Unexpected internal error while processing defsc. Please check your syntax." {} e))))))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.components))
(def ns-def (assoc (sci/copy-ns com.fulcrologic.fulcro.components sci-ns {:exclude [with-parent-context defsc defsc*]})
                       'with-parent-context (sci/copy-var with-parent-context sci-ns)
                       'defsc* (sci/copy-var defsc* sci-ns)
                       'defsc (sci/copy-var defsc sci-ns)))

(def sci-ns2 (sci/create-ns 'sci.configs.fulcro.component))
(def ns-def2 {'with-parent-context-fn (sci/copy-var with-parent-context-fn sci-ns2)})

(def namespaces {'com.fulcrologic.fulcro.components ns-def
                 'sci.configs.fulcro.component ns-def2})
