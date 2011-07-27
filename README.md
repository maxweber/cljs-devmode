# cljs-devmode

A development mode for ClojureScript. It allows you to develop Clojure
web applications in combination with ClojureScript seamlessly. You can
develop your normal Clojure web application (with Ring and Compojure
for example) and whenever you change a ClojureScript source file it is
automatically recompiled and you can test the changes in your web
browser.

## Usage

Follow the [Quick Start
Guide](https://github.com/clojure/clojurescript/wiki/Quick-Start) of
ClojureScript (the bootstrap step is required)

After bootstrapping ClojureScript get cljs-devmode:
      
    git clone git@github.com:maxweber/cljs-devmode.git

In the cljs-devmode folder invoke:

    lein cljs-devmode-bootstrap $CLOJURESCRIPT_HOME

If you haven't set the $CLOJURESCRIPT_HOME environment variable yet,
then use the full path to your ClojureScript installation.

The cljs-devmode-bootstrap leiningen plugin copies all necessary jar
files into the lib folder of $CLOJURESCRIPT_HOME to make this
development mode work.

### Start the ClojureScript compiler and cljs-devmode from the REPL

To run the hello sample of ClojureScript in development mode do the
following:

* Start the ./script/repl in the $CLOJURESCRIPT_HOME folder. 
* Then invoke this lines on the REPL:

    (require '[cljs.closure :as cljsc]) 
    (defn compile-fn []
          (cljsc/build "samples/hello/src" 
                       {:output-dir "samples/hello/out"
                        :output-to "samples/hello/hello.js"})) 
    (use 'cljs-devmode.core)
    (start-devmode "samples/hello" compile-fn)

* This brings up a Jetty server on port 9090, so open
  http://localhost:9090/hello-dev.html to see the hello sample of
  ClojureScript. On every request cljs-devmode checks, if any .cljs
  file inside samples/hello has changed. In the case of a change the
  above defined compile-fn is invoked. 
* So change the greeting message inside
  samples/hello/src/hello/core.cljs.
* When you refresh the http://localhost:9090/hello-dev.html you will
  see the new greeting message.

### cljs-devmode supports to automate the above steps

Take a look at the
[cljs-devmode-example](https://github.com/maxweber/cljs-devmode-example). In
the project folder of cljs-devmode-example you can invoke:

    lein cljs-devmode $CLOJURESCRIPT_HOME

This generates a cljs-devmode.sh file in the project folder. The
generated shell script starts the Clojure REPL in the
$CLOJURESCRIPT_HOME folder. Furthermore 'lein cljs-devmode' has also
generated a Clojure script, which is a string inside the shell script
and is passed to the Clojure REPL process for evaluation. This Clojure
script does the same as the script in the previous section. But the
paths are adapted to the current project folder (here the
cljs-devmode-example project). So if you execute the cljs-devmode.sh
script:

    chmod 755 cljs-devmode.sh 
    ./cljs-devmode

Then the ClojureScript compiler and the cljs-devmode are started
automatically.

The cljs-devmode-example also demonstrates how to use the
wrap-cljs-forward Ring middleware. This middleware takes care that all
request to /cljs/* (for example) are forwarded to localhost:9090,
where the cljs-devmode is running. So now you can develop your normal
Clojure web application and let ClojureScript do the JavaScript
part. Whenever you modify a .cljs file the whole ClojureScript project
is automatically recompiled and you can test the changes immediately
in your web browser.

'lein cljs-devmode' has some defaults / conventions for the folder
structure of the project. But all defaults can be changed through a
:cljs-devmode entry in the project.clj. These would be the default
settings:

    (defproject cljs-devmode-example
        ...
        :cljs-devmode {:dir "PROJECT_HOME/cljs"
                       :src-dir "PROJECT_HOME/cljs/src"
                       :output-dir "PROJECT_HOME/cljs/out")
                       :output-to "PROJECT_HOME/cljs/cljs-devmode-example.js")}

You do not have to specifiy the :cljs-devmode entry inside your
project.clj as long as the folder structure of your project follows
the conventions. Take a look at the folder structure of the
cljs-devmode-example. The whole ClojureScript project is in the cljs/
subfolder. The ClojureScript project itself adheres to the folder
structure of the ClojureScript samples. The :cljs-devmode entry can
also be leveraged to pass additional parameters to the ClojureScript
compiler. As you can see in the above example the :output-dir and
:output-to entries are settings for the ClojureScript compiler, every
addtional map entry will also be passed to the ClojureScript compiler.


## TODOs

This is a really primitive prototype for a ClojureScript development
mode. I only wrote it to get started with ClojureScript in one of my
Clojure web applications. 
There are many things which can be improved. For example the
generation of the .sh shell script is a little bit nasty ;-) You can
also let the JVM process of leiningen start the ClojureScript
compiler and the devmode via:

    lein cljs-devmode $CLOJURESCRIPT_HOME start

But then you will not see the output (stdout and stderr) of the
ClojureScript compiler and therefore you cannot read the helpful error
messages of the ClojureScript compiler.  Nevertheless there is no
automated way yet, to do a switch to "production mode" (e.g. invoke
the Google Closure Compiler in advanced mode and move the js file to
the public/cljs folder).

## License

Copyright (C) 2011 Maximilian Weber

Distributed under the Eclipse Public License, the same as Clojure.
