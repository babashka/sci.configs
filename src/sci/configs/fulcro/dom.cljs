(ns sci.configs.fulcro.dom
  (:require
   [sci.core :as sci]
   com.fulcrologic.fulcro.dom
   ["react-dom/server" :as react-dom-server]))

(defn render-to-str [e]
  ;; Re-write to use react-dom-server instead of relying on js/ReactDOMServer,
  ;; which I don't know how to get hold of in the SCI context.
  (react-dom-server/renderToString e))

(def sci-ns (sci/create-ns 'com.fulcrologic.fulcro.dom))
(def ns-def (assoc (sci/copy-ns com.fulcrologic.fulcro.dom sci-ns {:exclude ['render-to-str]})
                   'render-to-str (sci/copy-var render-to-str sci-ns)))

(def namespaces {'com.fulcrologic.fulcro.dom ns-def})