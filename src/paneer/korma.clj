(ns paneer.korma
  (:require [clojure.string :as str]))

(try 
  (require 'korma.core)
  (require 'paneer.core)
  (intern 'paneer.core 
          'execute 
          (eval '(fn [command]
                  (let [command (paneer.core/sql-string command)] 
                    (if (command? string) 
                      (korma.core/exec-raw command)
                      (korma.core/exec-raw (str "BEGIN; " (str/join " " command) " END;")))))))
  (catch Throwable T
    false))