(ns paneer.core-test
  (:refer-clojure :exclude [bigint boolean char double float time drop]) 
  (:require [clojure.test :refer :all]
            [paneer.core :refer :all]
            [paneer.db :as db]))

(-> (System/getenv)
    (get "TEST_DATABASE_URL")
    db/set-default-db!)

(deftest create*-test
  (is (= (create*) {:command :create :if-exists false :table nil :columns []}))
  (is (= (create* :if-exists true) 
         {:command :create :if-exists true :table nil :columns []})))

(deftest table*-test
  (is (= (table* (create*) :new-table)
         {:command :create :if-exists false :table "new-table" :columns []})))

(deftest column-test
  (is (= (column (create*) :id))
      {:command :create
       :if-exists false
       :table nil
       :columns [{:col-name "id"}]})
  (is (= (column (create*) :id :serial :not-null :primary-key)
         {:command :create 
          :if-exists false 
          :table nil 
          :columns [{:type "serial" :col-name "id" :options [:not-null :primary-key]}]})))

(deftest alter*-test
  (is (= (alter*) {:command :alter :if-exists false :table nil :columns []}))
  (is (= (alter* :if-exists true) 
      {:command :alter :if-exists true :table nil :columns []})))

(deftest drop*-test
  (is (= (drop*) {:command :drop :if-exists false :table nil :columns []}))
  (is (= (drop* :if-exists true) 
      {:command :drop :if-exists true :table nil :columns []}))) 

(deftest drop-column*-test
  (is (= (drop-column* (alter*) "name")
         {:command :alter-drop-column 
          :table nil 
          :if-exists false 
          :columns [{:col-name "name"}]})))

(deftest rename-to*-test
  (is (= (rename-to* (table* (alter*) "users") :bad_users)
         {:command :alter-rename
          :table "users" 
          :new-table "bad_users" 
          :if-exists false 
          :columns []})))

(deftest rename-column-to*-test
  (is (= (rename-column-to* (column (alter*) :name) :bad_name)
         {:command :alter-rename-column
          :table nil
          :if-exists false
          :columns [{:col-name "name"} {:col-name "bad_name"}]})))

(deftest add-column*-test
  (is (= (add-column* (alter*) "name")
         {:command :alter-create-column
          :table nil
          :if-exists false
          :columns [{:col-name "name"}]})))

(deftest create-test
  (is (= (create 
           (table 
             :users
             (serial :id :primary-key)
             (varchar :name 255)
             (varchar :email 255)))
         '(0)))
  (drop (table "users")))

(deftest create-if-not-exists-test
  (is (= (create-if-not-exists 
           (table 
             :users
             (serial :id :primary-key)
             (varchar :name 255)
             (varchar :email 255)))
         '(0)))
  (drop (table "users")))
 
(deftest drop-test
  (create (table "users" (serial :id :primary-key)))
  (is (= (drop
           (table :users))
         '(0))))

(deftest drop-if-exists-test
  (create (table "users" (serial :id :primary-key)))
  (is (= (drop-if-exists
           (table :users))
         '(0))))
