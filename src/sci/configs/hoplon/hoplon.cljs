(ns sci.configs.hoplon.hoplon
  (:refer-clojure :exclude [dosync defmacro])
  (:require [sci.core :as sci]
            [sci.ctx-store :as ctx-store]
            [clojure.set]
            [javelin.core :as j]
            [hoplon.core]
            [edamame.core :as e]
            [clojure.string :as str])
  (:require-macros [sci.configs.macros :as m]))

(def Exception js/Error)

;; Hoplon Interpolation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- ^{:from 'org.clojure/core.incubator} silent-read
  "Attempts to clojure.core/read a single form from the provided String, returning
  a vector containing the read form and a String containing the unread remainder
  of the provided String. Returns nil if no valid form can be read from the
  head of the String."
  [s]
  (try
    (let [r (-> s (e/source-reader))
          [v vs] (e/parse-next+string r)
          remainder (subs s (+ (str/index-of s vs) (count vs)))]
      [v remainder])
    (catch Exception e
      (js/console.error e)))) ; this indicates an invalid form -- the head of s is just string data

(defn- ^{:from 'org.clojure/core.incubator} terpol8*
  "Yields a seq of Strings and read forms."
  ([s atom?]
   (lazy-seq
    (if-let [[form rest] (silent-read (subs s (if atom? 2 1)))]
      (cons form (terpol8* (if atom? (subs rest 1) rest)))
      (cons (subs s 0 2) (terpol8* (subs s 2))))))
  ([^String s]
   (if-let [start (->> ["~{" "~("]
                       (map #(.indexOf s ^String %))
                       (remove #(== -1 %))
                       sort
                       first)]
     (lazy-seq (cons
                (subs s 0 start)
                (terpol8* (subs s start) (= \{ (.charAt s (inc start))))))
     [s])))


(defn terpol8 [s]
  (let [parts (remove #(= "" %) (terpol8* s))]
    (if (every? string? parts) s `(str ~@parts))))

(m/defmacro elem
  "Create an anonymous custom element."
  [bind & body]
  (let [[prepost & body] (if (map? (first body)) body (conj body nil))]
    `(fn [& args#] ~(or prepost {}) (let [~bind (hoplon.core/parse-args args#)] ~@body))))

(def sci-macroexpand-1 (delay (sci/eval-string* (ctx-store/get-ctx) "macroexpand-1")))
(defn macroexpand-1*
  ([expr] (macroexpand-1* {} expr))
  ([_env expr] (@sci-macroexpand-1 expr)))

(m/defmacro defelem
  "Defines an element function.

  An element function creates a DOM Element (parent) given two arguments:

    * `attrs` - a number of key-value pairs for attributes and their values
    * `kids` - a sequence of DOM Elements to be appended/used inside

  The returned DOM Element is itself a function which can accept more
  attributes and child elements."
  [name & forms]
  (let [[_ name [_ & [fdecl]]] (macroexpand-1* `(defn ~name ~@forms))
        [docstr & [bind & body]] (if (string? (first fdecl)) fdecl (conj fdecl nil))]
    `(def ^{:doc ~docstr} ~name (hoplon.core/elem ~bind ~@body))))

(m/defmacro defattr
  "Defines an attribute function.

  An element attribute is a function given three arguments:

    * `elem` - the target DOM Element containing the attribute
    * `key` - the attribute keyword or symbol
    * `value` - the attribute value

  The attribute function is called whenever the value argument changes."
  [name & forms]
  `(defmethod hoplon.core/do! ~name ~@forms))

(m/defmacro ^:private safe-deref [expr] `(deref (or ~expr (atom nil))))

(defn- parse-e [[tag & [head & tail :as args]]]
   (let [kw1? (comp keyword? first)
         mkkw #(->> (partition 2 %) (take-while kw1?) (map vec))
         drkw #(->> (partition 2 2 [] %) (drop-while kw1?) (mapcat identity))]
     (cond (map?     head) [tag head tail]
          (keyword? head) [tag (into {} (mkkw args)) (drkw args)]
           :else           [tag nil args])))

(m/defmacro loop-tpl
  "Template. Works identically to `for-tpl`, only expects a `:bindings`
  attribute to accomodate the HTML HLisp representation:

    (loop-tpl :bindings [x xs] ...)
  "
  [& args]
  (let [[_ {[bindings items] :bindings} [body]] (parse-e (cons '_ args))]
    `(hoplon.core/loop-tpl* ~items
        (fn [item#] (j/cell-let [~bindings item#] ~body)))))

(m/defmacro for-tpl
  "Template. Accepts a cell-binding and returns a cell containing a sequence of
  elements:

    (for-tpl [x xs] (span x))
  "
  [[bindings items] body]
  `(hoplon.core/loop-tpl* ~items (fn [item#] (j/cell-let [~bindings item#] ~body))))

(m/defmacro if-tpl
  "Template. Accepts a `predicate` cell and returns a cell containing either
  the element produced by `consequent` or `alternative`, depending on the value
  of the predicate:

    (if-tpl predicate (span \"True\") (span \"False\"))
  "
  [predicate consequent & [alternative]]
  `(let [con# (delay ~consequent)
         alt# (delay ~alternative)
         tpl# (fn [p#] (hoplon.core/safe-deref (if p# con# alt#)))]
     ((j/formula tpl#) ~predicate)))

(m/defmacro when-tpl
  "Template. Accepts a `predicate` cell and returns a cell containing either
  the element produced by `consequent` or nothing, depending on the value of
  the predicate:

    (when-tpl predicate (span \"Value\"))
  "
  [predicate & body]
  `(hoplon.core/if-tpl ~predicate (do ~@body)))

(m/defmacro cond-tpl
  "Template. Accepts a number of `clauses` cell-template pairs and returns a
  cell with the value produced by the matching clause:

    (cond-tpl
      clause-a (span \"A\")
      clause-b (span \"B\")
      :else    (span \"Default\"))
  "
  [& clauses]
  (let [[conds tpls] (apply map vector (partition 2 clauses))
        syms1        (repeatedly (count conds) gensym)
        syms2        (repeatedly (count conds) gensym)]
    `(let [~@(interleave syms1 (map (fn [x] `(delay ~x)) tpls))
           tpl# (fn [~@syms2] (hoplon.core/safe-deref (cond ~@(interleave syms2 syms1))))]
       ((j/formula tpl#) ~@conds))))

(m/defmacro case-tpl
  "Template. Accepts an `expr` cell and a number of `clauses` and returns a
  cell with the value produced by the matching clause:

    (case-tpl expr
      :a (span \"A\")
      :b (span \"B\")
      (span \"Default\"))

  "
  [expr & clauses]
  (let [[cases tpls] (apply map vector (partition 2 clauses))
        default      (when (odd? (count clauses)) (last clauses))
        syms         (repeatedly (inc (count cases)) gensym)]
    `(let [~@(interleave syms (map (fn [x] `(delay ~x)) (conj tpls default)))
           tpl# (fn [expr#] (hoplon.core/safe-deref (case expr# ~@(interleave cases syms) ~(last syms))))]
       ((j/formula tpl#) ~expr))))

;; DOM Macros ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(m/defmacro with-dom
  "Evaluates the body after elem has been inserted into the DOM."
  [elem & body]
  `(hoplon.core/when-dom ~elem (fn [] ~@body)))

(m/defmacro with-timeout
  "Evaluates the body after msec milliseconds, asynchronously. Returns the
  timeout ID which can be used to cancel the operation (see js/clearTimeout)."
  [msec & body]
  `(js/setTimeout (fn [] ~@body) ~msec))

(m/defmacro with-interval
  "Evaluates the body every msec milliseconds, asynchronously. Returns the
  interval ID which can be used to cancel the operation (see js/clearInterval)."
  [msec & body]
  `(js/setInterval (fn [] ~@body) ~msec))

(m/defmacro with-animation-frame
  "Evaluates the body before the next browser repaint as requestAnimationFrame."
  [& body]
  `(.requestAnimationFrame js/window (fn [] ~@body)))

(m/defmacro with-init!
  "Evaluates the body after Hoplon has completed constructing the page."
  [& body]
  `(hoplon.core/add-initfn! (fn [] ~@body)))

(m/defmacro text
  "Creates a DOM Text node and binds its text content to a formula created via
  string interpolation, so the Text node updates with the formula."
  [form]
  (let [i (if-not (string? form) form (terpol8 form))]
    (if (string? i)
      `(.createTextNode js/document ~i)
      `(j/with-let [t# (.createTextNode js/document "")]
         (j/cell= (set! (.-nodeValue t#) ~i))))))

(def hns (sci/create-ns 'hoplon.core nil))

(def hoplon-core-namespace (assoc (sci/copy-ns hoplon.core hns)
                                  'text (sci/copy-var text hns)
                                  'elem (sci/copy-var elem hns)
                                  'defelem (sci/copy-var defelem hns)
                                  'defattr (sci/copy-var defattr hns)
                                  'safe-deref (sci/copy-var safe-deref hns)
                                  'loop-tpl (sci/copy-var loop-tpl hns)
                                  'for-tpl (sci/copy-var for-tpl hns)
                                  'if-tpl (sci/copy-var if-tpl hns)
                                  'when-tpl (sci/copy-var when-tpl hns)
                                  'cond-tpl (sci/copy-var cond-tpl hns)
                                  'case-tpl (sci/copy-var case-tpl hns)
                                  'with-dom (sci/copy-var with-dom hns)
                                  'with-timeout (sci/copy-var with-timeout hns)
                                  'with-interval (sci/copy-var with-interval hns)
                                  'with-animation-frame (sci/copy-var with-animation-frame hns)
                                  'with-init! (sci/copy-var with-init! hns)))

(def config {:namespaces {'hoplon.core hoplon-core-namespace}})
