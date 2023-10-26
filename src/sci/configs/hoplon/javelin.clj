(ns sci.configs.hoplon.javelin)

(defmacro ^:private with-let*
  "Binds resource to binding and evaluates body.  Then, returns
  resource.  It's a cross between doto and with-open."
  [[binding resource] & body]
  `(let [~binding ~resource] ~@body ~binding))
