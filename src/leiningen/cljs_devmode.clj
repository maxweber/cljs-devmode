(ns leiningen.cljs-devmode
  (:use clojure.java.io
        leiningen.deps
        leiningen.jar))

(defn dest-dir [clojurescript-path]
  (file (file clojurescript-path) "lib/"))

(defn- copy-deps [dir]
  (let [libs (file "lib/dev")]
    (doall (map #(let [name (.getName %)
                       dest-file (file dir name)]
                   (copy % dest-file))
                (remove #(= "clojure-1.2.1.jar" (.getName %)) (.listFiles libs))))))

(defn- build-and-copy-jar [project dest-dir]
  (let [jar-name (get-jar-filename project)
        jar-file (file jar-name)]
    (jar project)
    (copy jar-file (file dest-dir (.getName jar-file)))))

(defn cljs-devmode
  [project & [clojurescript-path]]
  (deps project)
  (let [dest (dest-dir clojurescript-path)]
    (copy-deps dest)
    (build-and-copy-jar project dest)))

(comment (cljsc/build "samples/hello/src" {:output-dir "samples/hello/out"
                                        :output-to "samples/hello/hello.js"}))
