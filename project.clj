(defproject bartbot "0.1.0"
  :url "https://github.com/taeold/bartbot"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.2"]
                 [cheshire "5.6.1"]
                 [clj-http "2.2.0"]
                 [compojure "1.5.0"]
                 [levand/immuconf "0.1.0"]
                 [prismatic/schema "1.1.1"]
                 [ring.middleware.logger "0.5.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :jvm-opts ["-Xmx1g"
             "-server"
             "-Dlog4j.configuration=file:resources/log4j.properties"]
  :ring {:port 8080
         :handler bartbot.service/handler}
  :profiles {:dev
             {:dependencies
              [[javax.servlet/servlet-api "2.5"]]}}
  :aliases {"server" ["ring" "server-headless"]})
