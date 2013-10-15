(ns paneer.db
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as j]))

(defonce __default (atom {}))

(defn from-korma-db-definition
  "Returns connection info from Korma DB definition"
  [{:keys [db-spec]}]
  db-spec)

(defn set-default-db!
  "Sets the default db for future transactions"
  [db]
  (reset! __default db))
