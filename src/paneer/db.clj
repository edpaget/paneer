(ns paneer.db
  (:require [clojure.string :as str]
            [korma.core :refer [exec-raw]]
            [paneer.engine :refer [eval-query]]))

(defn execute
  "Executes the command against the default Korma connection. Accepted options are
    :db - specifies a different Korma database connection to use
    :sql - return only the sql that would have been executed"
  [command & opts]
  (let [command (eval-query command)] 
    (cond 
      (:sql opts) command
      (:db opts) (exec-raw (:db opts) command)
      (vector? command) (flatten (map exec-raw command))
      true (exec-raw command))))
