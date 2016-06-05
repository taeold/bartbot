(defproject bartbot "0.1.0"
  :url "https://github.com/taeold/bartbot"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.0"]
                 [ring.middleware.logger "0.5.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :jvm-opts ["-Xmx1g"
             "-server"
             "-Dlog4j.configuration=file:resources/log4j.properties"]
  :ring {:port 8080
         :handler bartbot.service/handler}
  :aliases {"server" ["ring" "server-headless"]})
