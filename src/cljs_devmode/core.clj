(ns cljs-devmode.core
  (:use ring.util.response
        ring.middleware.file
        clojure.set
        ring.adapter.jetty)
  (:import java.io.File))

(defn clojurescript-source-file?
  "Returns true if file is a normal file with a .cljs extension."
  [^File file]
  (and (.isFile file)
       (.endsWith (.getName file) ".cljs")))

(defn find-clojurescript-sources-in-dir
  "Searches recursively under dir for ClojureScript source files (.cljs).
  Returns a sequence of File objects, in breadth-first sort order."
  [^File dir]
  ;; Use sort by absolute path to get breadth-first search.
  (sort-by #(.getAbsolutePath %)
           (filter clojurescript-source-file? (file-seq dir))))

(defn handler [request]
  (response "ClojureScript devmode"))

(def src-files (atom {}))

(defn new-or-deleted-files? [files1 files2]
  (let [s1 (into #{} files1)
        s2 (into #{} files2)]
    (not (= s1 s2))))

(defn changes? [dir]
  (let [files (into {} (map (fn [file]
                         [(.getAbsolutePath file)
                          (.lastModified file)])
                       (find-clojurescript-sources-in-dir
                        (File. dir))))]
    (if (new-or-deleted-files? (keys files) (keys @src-files))
      (boolean (reset! src-files files))
      (if (some (fn [[path timestamp]]
                  (not (= (get @src-files path) timestamp))) files)
        (boolean (reset! src-files files))
        false))))

(defn wrap-compile [handler dir compile-fn]
  (fn [request]
    (when (changes? dir)
      (println "Compiling ClojureScript files ...")
      (compile-fn)
      (println "Done."))
    (handler request)))

(defn app [dir compile-fn]
     (-> handler
         (wrap-file dir)
         (wrap-compile dir compile-fn)))

(def server nil)

(defn start-devmode [dir compile-fn]
  (alter-var-root (var server)
                  (fn [v]
                    (run-jetty (app dir compile-fn) {:port 9090 :join? false}))))

(defn stop-devmode []
  (when server
    (.stop (.get server))))

(comment
  (cljsc/build "samples/hello/src" {:output-dir "samples/hello/out"
                                    :output-to "samples/hello/hello.js"}))
