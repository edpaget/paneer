(ns paneer.korma
  (:refer-clojure :exclude [bigint boolean char double float time drop alter]) 
  (:use korma.core
        paneer.core)
  (:require [clojure.string :as str]))

(defn exec-korma*
  [command]
  (let [command (sql-string command)] 
    (if (string? command) 
      (exec-raw command)
      (map #(exec-raw %) command))))

(defmacro exec-korma
  "Runs Command using Korma's connection."
  [command]
  (let [command (rest (macroexpand command))]
    `(exec-korma* ~@command)))
