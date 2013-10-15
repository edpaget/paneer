(defproject paneer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GNU General Public License"
            :url "https://www.gnu.org/licenses/gpl-3.0.html"}
  :codox {:exclude [paneer.engine]
          :src-dir-uri "https://github.com/edpaget/paneer/blob/master/"
          :src-linenum-anchor-prefix "L"}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]
                                  [org.clojure/java.jdbc "0.3.0-alpha5"]
                                  [postgresql/postgresql "8.4-702.jdbc4"]
                                  [com.h2database/h2 "1.3.164"]]
                   :plugins [[codox "0.6.6"]]}})
