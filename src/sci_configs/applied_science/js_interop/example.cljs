(ns sci-configs.applied-science.js-interop.example
  (:require [sci-configs.applied-science.js-interop.config :as js-interop-config]
            [sci.core :as sci]))

(def sci-opts (merge
               ;; your options in this map
               {}
               js-interop-config/config))

(def sci-ctx (sci/init sci-opts))

(sci/eval-string* sci-ctx "(require '[applied-science.js-interop :as j]) (j/assoc! #js {} :foo 1)")
