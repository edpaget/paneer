(ns paneer.korma)

(try 
  (require 'korma.core)
  (require 'paneer.core)
  (intern 'paneer.core 
          'execute 
          (eval '(fn [command]
                   (korma.core/exec-raw (paneer.core/sql-string command)))))
  (catch Throwable T
    false))