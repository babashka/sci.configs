(ns sci.configs.fulcro.fulcro-sci-helpers)

(defn error
  "Replace cljs.analyzer/error so that we don't to pull in this huge dependency"
  ([env msg] (error env msg nil))
  ([{:keys [line file] :as env} msg cause]
   (ex-info (cond-> msg
              line (str " at line " line)
              file (str " in " file))
            env
            cause)))