(defproject paneer "0.2.0-SNAPSHOT"
  :description "A small library for managing SQL tables intended to be used alongside Korma, or Drift"
  :url "https://edpaget.github.io/paneer"
  :license {:name "GNU General Public License"
            :url "https://www.gnu.org/licenses/gpl-3.0.html"}
  :codox {:exclude [paneer.engine paneer.korma]
          :src-dir-uri "https://github.com/edpaget/paneer/blob/master/"
          :src-linenum-anchor-prefix "L"}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]
                                  [org.clojure/java.jdbc "0.3.0-alpha5"]
                                  [bultitude "0.2.2"]
                                  [korma "0.3.0-RC6"]
                                  [postgresql/postgresql "8.4-702.jdbc4"]
                                  [com.h2database/h2 "1.3.164"]]
                   :plugins [[codox "0.6.6"]
                             [lein-localrepo "0.5.2"]]}})
