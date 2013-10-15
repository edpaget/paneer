(ns paneer.db
  (:require [clojure.string :as str]))

(defonce __default (atom {}))

(defn- db-info
  [db-type]
  (cond 
    (= db-type "postgres") {:classname "org.postgresql.Driver"
                            :subprotocol "postgresql"}
    (= db-type "h2") {:classname "org.h2.Driver"
                      :subprotocol "h2"}))

(defn db-spec-from-uri
  "Creates a db-spec map from a URI in the form db-type://user:pass@host:port/db"
  [uri]
  (let [protocol (get (str/split uri #"://") 0)
        uri (java.net.URI. uri)
        host (.getHost uri)
        port (.getPort uri)
        path (.getPath uri)
        username (.getUserInfo uri)
        subname (str "//" host (when-not (= -1 port) (str ":" port)) path)
        [username password] (when username (str/split username #":"))
        auth (when username {:user username :password password})]
    (merge auth (db-info protocol) {:subname subname})))

(defn db-spec-from-korma-db
  "Returns connection info from Korma DB definition"
  [{:keys [db-spec]}]
  db-spec)

(defn set-default-db!
  "Sets the default db for future transactions"
  [db]
  (reset! __default db))