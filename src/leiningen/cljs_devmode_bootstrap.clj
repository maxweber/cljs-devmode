(ns leiningen.cljs-devmode-bootstrap
  (:use clojure.java.io
        leiningen.deps
        leiningen.jar
        [leiningen.cljs-devmode :only [check-clojurescript-home-param]]))

(defn- dest-dir [clojurescript-path]
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

(defn cljs-devmode-bootstrap
  [project & [clojurescript-path]]
  (when-let [clojurescript-path (check-clojurescript-home-param clojurescript-path)]
    (if-not (= (:name project) "cljs-devmode")
      (binding [*out* *err*]
        (println "Error: cljs-devmode-bootstrap must be invoked in the root folder of the cljs-devmode project (get the sources from GitHub: https://github.com/maxweber/cljs-devmode)"))
      (do
        (deps project)
        (let [dest (dest-dir clojurescript-path)]
          (copy-deps dest)
          (build-and-copy-jar project dest))))))

(comment (cljsc/build "samples/hello/src" {:output-dir "samples/hello/out"
                                        :output-to "samples/hello/hello.js"}))
