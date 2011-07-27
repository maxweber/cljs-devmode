(ns cljs-devmode.ring-middleware
  (:require [clj-http.client :as http]))

(defn wrap-cljs-forward
  ([handler prefix server-url]
     (fn [request]
       (let [{:keys [uri request-method]} request]
         (if (and (= request-method :get) uri (.startsWith uri prefix))
           (let [url (str server-url (.replaceFirst uri prefix ""))
                 headers (:headers request)]
             (http/request {:method :get :url url
                            :headers headers}))
           (handler request)))))
  ([handler prefix] (wrap-cljs-forward handler prefix "http://localhost:9090")))
