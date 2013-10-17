(ns paneer.core-korma-test
  (:refer-clojure :exclude [bigint boolean char double float time drop alter]) 
  (:use clojure.test
        korma.db
        paneer.core))

(defdb pg (postgres {:user "edward"
                     :password "blah"
                     :db "test"}))

(deftest execute-test
  (drop-if-exists (table :users))
  (is (= (create 
           (table :users 
                  (serial :id :primary-key)
                  (varchar :name 255 :not-null)
                  (varchar :email 255)))
         '(0)))
  (drop (table :users)))
