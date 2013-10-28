(ns paneer.db
  (:require [clojure.string :as str]
            [korma.core :refer [exec-raw]]
            [paneer.engine :refer [eval-query]]))

(defn execute
  "Executes the command against the default Korma connection. Accepted options are
    :raw - booleans whether to evaluate command using paneer to try to execute as string
      the default is false
    :db - specifies a different Korma database connection to use"
  [command & opts]
  (cond 
    (and (:db opts) (:raw opts)) (exec-raw (:db opts) command)
    (:raw opts) (exec-raw command)
    (:db opts) (exec-raw (:db opts) (eval-query command))
    true (exec-raw (eval-query command))))
