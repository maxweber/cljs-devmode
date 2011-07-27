(ns leiningen.cljs-devmode
  (:use clojure.java.io)
  (:import [java.lang Process Runtime]
           java.io.StringReader))

(def cljs-command
     ["java" "-server" "-Xmx2G" "-Xms2G" "-Xmn256m"
      "-cp" "lib/*:src/clj:src/cljs"
      "clojure.main" "-e"])

(defn- build-cljs-invoke [script]
  (conj cljs-command (apply str script)))

;TODO: How can the stdout of the other JVM be redirected to the stdout
;of this JVM (clojure.java.shell doesn't support to get the
;InputStream from the inner java.lang.Process directly yet, so we have
;to use Runtime.exec here) 
(defn- start-clojurescript [clojurescript-home script]
  (let [cljs-invoke (build-cljs-invoke script)
        runtime (Runtime/getRuntime)
        process (.exec runtime ^"[Ljava.lang.String;" (into-array cljs-invoke)
                       ^"[Ljava.lang.String;" (into-array String [])
                       (as-file clojurescript-home))]
    (with-open [err (.getErrorStream process)]
      (slurp err))))

(defn generate-script [clojurescript-home script]
  (str "#!/bin/sh\n"
       "cd " clojurescript-home "\n"
       (apply str (interpose " " cljs-command))
       " '"
       (apply str script)
       "'"))

(def cljsc-bin-path "bin/cljsc")

(defn is-clojurescript-home? [clojurescript-home]
  (let [cljsc-bin (file (file clojurescript-home) cljsc-bin-path)]
    (.exists cljsc-bin)))

(defn check-clojurescript-home-param [clojurescript-home]
  (if-not clojurescript-home
    "Error: Please provide the ClojureScript home path as first argument."
    (when-not (is-clojurescript-home? clojurescript-home)
      (str "Error: The given ClojureScript home path '"
           clojurescript-home
           "' is not a ClojureScript installation "
           "(" cljsc-bin-path " is missing.)"))))

(defn cljs-devmode
  [project & [clojurescript-home mode]]
  (if-let [error (check-clojurescript-home-param clojurescript-home)]
    (println error)
    (if (and mode (not (= mode "start")))
      (println "Error: The only supported mode at the moment is 'start', which starts the ClojureScript compiler process in another JVM from this JVM via Runtime/exec \"java.exe ...\" (you will not see the stdout or stderr of the ClojureScript compiler process).")
      (let [root-dir (:root project)
            dir (str root-dir "/cljs")
            project-name (:name project)
            defaults {:dir dir
                      :src-dir (str dir "/src")
                      :output-dir (str dir "/out")
                      :output-to (str dir "/" project-name ".js")}
            cljs-devmode-opts (:cljs-devmode project {})
            options (merge defaults cljs-devmode-opts)
            cljs-build-opts (dissoc defaults :dir :src-dir)
            dir (:dir options)
            src-dir (:src-dir options)
            script `((require 'cljs.closure)
                     (defn user/compile-fn [] (cljs.closure/build ~src-dir
                                                                  ~cljs-build-opts))
                     (require 'cljs-devmode.core)
                     (cljs-devmode.core/start-devmode ~dir user/compile-fn))]
        (if (= mode "start")
          (do
            (println "cljs-devmode started. Options: " options)
            (println (start-clojurescript clojurescript-home script)))
          (spit "cljs-devmode.sh" (generate-script clojurescript-home script)))))))
