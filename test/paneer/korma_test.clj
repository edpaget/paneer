(ns paneer.korma-test
  (:refer-clojure :exclude [bigint boolean char double float time drop alter]) 
  (:use clojure.test
        korma.db
        paneer.core)
  (:require [clojure.string :as str]))

(defn start-db
  []
  (require 'paneer.korma)
  (let [uri (java.net.URI. (get (System/getenv) "TEST_DATABASE_URL"))
        [user password] (str/split (.getUserInfo uri) #":")
        path (apply str (clojure.core/drop 1 (.getPath uri)))]
    (defdb pg (postgres {:user user
                         :password password
                         :db path}))))

(deftest execute-test
  (start-db)
  (drop-if-exists (table :users))
  (is (= (create 
           (table :users 
                  (serial :id :primary-key)
                  (varchar :name 255 :not-null)
                  (varchar :email 255)))
         '(0)))
  (drop (table :users)))
