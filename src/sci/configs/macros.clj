(ns sci.configs.macros
  (:refer-clojure :exclude [defmacro]))

(def ^:dynamic *debug* false)

(defn add-macro-args [[args & body]]
  (list* (into '[&form &env] args) body))

(clojure.core/defmacro defmacro [name & body]
  (let [[?doc body] (if (and (string? (first body))
                            (> (count body) 2))
                      [(first body) (rest body)]
                      [nil body])
        bodies (if (vector? (first body))
                 (list body)
                 body)]
    #_(when *debug* (.println System/err (with-out-str (clojure.pprint/pprint bodies))))
    `(defn ~(vary-meta name assoc :sci/macro true)
       ~@(when ?doc [?doc])
       ~@(map add-macro-args bodies))))

#_(binding [*debug* true]
  (.println System/err (str (macroexpand '(defmacro my-stuff "docstring" [& args] x y z)))))
