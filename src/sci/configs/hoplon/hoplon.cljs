(ns sci.configs.hoplon.hoplon
  (:refer-clojure :exclude [dosync defmacro])
  (:require [sci.core :as sci]
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
                                  'text (sci/copy-var text hns)))

(def config {:namespaces {'hoplon.core hoplon-core-namespace}})
