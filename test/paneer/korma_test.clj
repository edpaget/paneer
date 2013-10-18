(ns paneer.korma-test
  (:refer-clojure :exclude [bigint boolean char double float time drop alter]) 
  (:use clojure.test
        korma.db
        paneer.core
        paneer.korma)
  (:require [clojure.string :as str]))

(defn start-db
  []
  (let [uri (java.net.URI. (get (System/getenv) "TEST_DATABASE_URL"))
        [user password] (str/split (.getUserInfo uri) #":")
        path (apply str (clojure.core/drop 1 (.getPath uri)))]
    (defdb pg (postgres {:user user
                         :password password
                         :db path}))))

(deftest execute-test
  (start-db)
  (is (= (exec-korma (drop-if-exists (table :users)))
         '(0)))
  (is (= (exec-korma (create 
                       (table :users 
                              (serial :id :primary-key)
                              (varchar :name 255 :not-null)
                              (varchar :email 255))))
         '(0)))
  (is (= (exec-korma (alter
                       (table :users
                              (add-column 
                                (integer :lucky-no :unique)
                                (varchar :address 255 :not-null)))))
         '((0) (0))))
  (is (= (exec-korma (drop (table :users)))
         '(0))))
