(ns sci.configs.macros
  (:refer-clojure :exclude [defmacro]))

(defn add-macro-args [[args body]]
  ;; (prn :args args :body body)
  (cons (into '[&form &env] args) body))

(clojure.core/defmacro defmacro [name & body]
  (let [[?doc body] (if (and (string? (first body))
                            (> (count body) 2))
                      [(first body) (rest body)]
                      [nil body])
        bodies (if (vector? (first body))
                 (list body)
                 body)]
    `(defn ~(vary-meta name assoc :sci/macro true)
       ~@(when ?doc [?doc])
       ~@(map add-macro-args bodies))))

#_(defmacro macroexpand-all
  "Fully expand all CLJS macros contained in form."
  [form]
  [&env form])
