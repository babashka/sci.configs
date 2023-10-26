(ns sci.configs.macros
  (:refer-clojure :exclude [defmacro]))

(defn add-macro-args [[args body]]
  (list (into '[&form &env] args) body))

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

