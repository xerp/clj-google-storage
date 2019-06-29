(defproject clj-google-storage "2.2"
  :description "Google storage api"
  :url "https://github.com/xerp/clj-google-storage"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.cemerick/url "0.1.1"]
                 [clj-http "3.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-google "1.6"]]
  :repositories [["releases" {:url           "https://clojars.org/repo"
                              :username      :env/CLOJAR_USERNAME
                              :password      :env/CLOJAR_PASSWORD
                              :sign-releases false}]]
  :repl-options {:init-ns clj-google-storage.core})
