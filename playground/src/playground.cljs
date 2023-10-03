(ns playground
  "Build CodeMirror editor with SCI evaluation for the SCI Playground."
  (:require
   [sci.core :as sci]
   [clojure.string :as str]

   ;; All the configs
   sci.configs.applied-science.js-interop
   sci.configs.cljs.pprint
   sci.configs.cljs.test
   ;sci.configs.clojure.test
   sci.configs.fulcro.fulcro
   sci.configs.funcool.promesa
   sci.configs.mfikes.cljs-bean
   sci.configs.re-frame.re-frame
   sci.configs.reagent.reagent
   sci.configs.reagent.reagent-dom-server
   sci.configs.tonsky.datascript
   ; sci.configs.clojure-1-11
   
   ;; Code editor
   ;; Inspiration: https://github.com/nextjournal/clojure-mode/blob/main/demo/src/nextjournal/clojure_mode/demo.cljs
   ["@codemirror/commands" :refer [history historyKeymap]]
   ["@codemirror/language" :refer [#_foldGutter
                                   syntaxHighlighting
                                   defaultHighlightStyle]]
   ["@codemirror/state" :refer [EditorState]]
   ["@codemirror/view" :as view :refer [EditorView lineNumbers showPanel]]

  
   ;; JS deps for re-export to sci
   ["react" :as react]
   ["react-dom" :as react-dom]

   [nextjournal.clojure-mode :as cm-clj]

   ;; Used libs
   [promesa.core :as p]
   ))

;; Necessary to avoid the error 'Attempting to call unbound fn: #'clojure.core/*print-fn*'
;; when calling `println` inside the evaluated code
(enable-console-print!)
(sci/alter-var-root sci/print-fn (constantly *print-fn*))
(sci/alter-var-root sci/print-err-fn (constantly *print-err-fn*))

;; ------------------------------------------------------------ SCI eval

(def all-configs ; vars so that we can extract ns info
  [#'sci.configs.applied-science.js-interop/config
   #'sci.configs.cljs.pprint/config
   #'sci.configs.cljs.test/config
   ;#'sci.configs.clojure.test/config
   #'sci.configs.fulcro.fulcro/config
   #'sci.configs.funcool.promesa/config
   #'sci.configs.mfikes.cljs-bean/config
   #'sci.configs.re-frame.re-frame/config
   #'sci.configs.reagent.reagent/config
   #'sci.configs.reagent.reagent-dom-server/config
   #'sci.configs.tonsky.datascript/config])

(def sci-ctx
  (->> all-configs
       (map deref)
       (reduce
        sci/merge-opts
        (sci/init {:classes {'js js/globalThis :allow :all}
                   :js-libs {"react" react
                             "react-dom" react-dom}}))))

(defn eval-code
  ([code]
   (try (sci/eval-string* sci-ctx code)
        (catch :default e
          (try (js/console.log "Evaluation failed:" (ex-message e)
                               (some-> e ex-data clj->js))
               (catch :default _))
          {::error (str (.-message e)) :data (ex-data e)}))))

(defn eval-all [on-result  x]
  (on-result (some->> (.-doc (.-state x)) str eval-code))
  true)

(defn sci-extension [on-result]
  (.of view/keymap
       #js [#js {:key "Mod-Enter" ; Cmd or Ctrl
                 :run (partial eval-all on-result)}]))

;; ------------------------------------------------------------ Code editor

(defn mac? []
  (some? (re-find #"(Mac)|(iPhone)|(iPad)|(iPod)" js/navigator.platform)))

(defn output-panel-extension
  "Display a panel below the editor with the output of the
   last evaluation (read from the passed-in `result-atom`)"
  [result-atom]
  (let [dom (js/document.createElement "div")]
    (add-watch result-atom :output-panel
               (fn [_ _ _ new]
                 (if (::error new)
                   (do
                     (.add (.-classList dom) "error")
                     (set! (.-textContent dom) (str "ERROR: " (::error new)
                                                    (some->> new :data pr-str (str " ")))))
                   (do
                     (.remove (.-classList dom) "error")
                     (set! (.-textContent dom) (str ";; => " (pr-str new)))))))
    (set! (.-className dom) "cm-output-panel")
    (set! (.-textContent dom)
          (str "Press "
               (if (mac?) "Cmd" "Ctrl")
               "-Enter in the editor to evaluate it. Return value will show up here."))
    (.of showPanel
         (fn [_view] #js {:dom dom}))))

(def theme
  (.theme
   EditorView
   #js {".cm-output-panel.error" #js {:color "red"}}))

(defonce extensions
  #js[theme
      (history)
      (syntaxHighlighting defaultHighlightStyle)
      (view/drawSelection)
      (lineNumbers)
      (.. EditorState -allowMultipleSelections (of true))
      cm-clj/default-extensions
      (.of view/keymap cm-clj/complete-keymap)
      (.of view/keymap historyKeymap)])

(defn bind-editor! [el code]
  {:pre [el code]}
  (let [target-el (js/document.createElement "div")
        last-result (atom nil)
        exts (.concat extensions
                      #js [(sci-extension (partial reset! last-result))
                           (output-panel-extension last-result)])]
    (.replaceWith el target-el)
    (new EditorView
         #js {:parent target-el
              :state (.create EditorState #js {:doc code
                                               :extensions exts})})))

(defn list-libraries [all-config-vars]
  (->> all-config-vars
       (map (comp name :ns meta))
       (map #(clojure.string/replace % #"^sci\.configs\.[\w-]+\." ""))
       (remove #{"pprint" "test"})
       sort
       (str/join ", ")))

(defn gist-json->code [json]
  (->> json
       .-files
       js/Object.values
       seq
       (map (fn [o] (js->clj o :keywordize-keys true)))
       (filter (comp #{"Clojure"} :language)) ; incl. clj, cljs, cljc
       (sort-by :filename) ; we started with a map, which has no natural order
       (map #(do (assert (not (:truncated %)) "Can't handle truncated files")
                 (str ";; " (:filename %) "\n" (:content %))))
       (str/join "\n;;---\n")))

(defn async-fetch-gist [gist-id]
  (p/let [resp (js/fetch (str "https://api.github.com/gists/" gist-id)
                         {:headers {"Accept" "application/vnd.github+json"
                                    "X-GitHub-Api-Version" "2022-11-28"}})
          _ (when-not (.-ok resp) (throw (ex-info (str "Bad HTTP status " 
                                                       (.-status resp) " " 
                                                       (.-statusText resp)) 
                                                  {})))
          json (.json resp)
          code (gist-json->code json)]
    (if (seq code)
      code
      "; No Clojure code found in the gist.")))

(defn ^:export init []
  (let [code-el (js/document.getElementById "code")
        code (.-textContent code-el)
        libs-el (js/document.getElementById "libs")]
    (set! (.-textContent libs-el) (list-libraries all-configs))
    (if-let [gist-id
             (->> js/document .-location .-search
                  (re-find #"[?&]gist=(\w+)")
                  second)]
      (do
        (set! (.-textContent code-el) "Loading gist...")
        (-> (async-fetch-gist gist-id)
            (p/then #(let [res (eval-code %)]
                       (println "Initial evaluation => " res)
                       (when (::error res)
                         (set! (.-textContent (js/document.getElementById "app"))
                               (str "Auto-evaluating the gist failed. Cause: " (::error res))))
                       (bind-editor! code-el %)))
            (p/catch #(set! (.-textContent code-el) (str "Loading gist FAILED: " %)))))
      (bind-editor! code-el code)))
  (println "Init run"))

(defn ^:export reload []
  (println "Reload run (noop)"))
