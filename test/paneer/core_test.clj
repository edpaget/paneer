(ns paneer.core-test
  (:refer-clojure :exclude [bigint boolean double float]) 
  (:require [clojure.test :refer :all]
            [paneer.core :refer :all]
            [paneer.db :refer :all]
            [clojure.string :as str]
            [korma.db :refer :all :exclude [transaction]]))

(defn start-db
  []
  (let [uri (java.net.URI. (get (System/getenv) "TEST_DATABASE_URL"))
        [user password] (str/split (.getUserInfo uri) #":")
        path (apply str (clojure.core/drop 1 (.getPath uri)))]
    (defdb pg (postgres {:user user
                         :password password
                         :db path}))))

(start-db)

(defn create-users
  [] 
  (create-table "users" (serial :id :primary-key)))

(defn drop-users
  []
  (drop-table :users))

(deftest create*-test
  (is (= (create*) {:command :create-table :if-exists false :table nil :columns []}))
  (is (= (create* :if-exists true) 
         {:command :create-table :if-exists true :table nil :columns []})))

(deftest table-test
  (is (= (table (create*) :new-table)
         {:command :create-table :if-exists false :table "new-table" :columns []})))

(deftest column-test
  (is (= (column (create*) :id))
      {:command :create-table
       :if-exists false
       :table nil
       :columns [{:col-name "id"}]})
  (is (= (column (create*) :id :serial :not-null :primary-key)
         {:command :create-table 
          :if-exists false 
          :table nil 
          :columns [{:type "serial" :col-name "id" :options [:not-null :primary-key]}]})))

(deftest alter*-test
  (is (= (alter*) {:command :alter-table :if-exists false :table nil :columns []}))
  (is (= (alter* :if-exists true) 
         {:command :alter-table :if-exists true :table nil :columns []})))

(deftest drop*-test
  (is (= (drop*) {:command :drop-table :if-exists false :table nil :columns []}))
  (is (= (drop* :if-exists true) 
         {:command :drop-table :if-exists true :table nil :columns []}))) 

(deftest drop-column*-test
  (is (= (drop-column* (alter*) "name")
         {:command :alter-drop-column 
          :table nil 
          :if-exists false 
          :columns [{:col-name "name"}]})))

(deftest rename-to*-test
  (is (= (rename-to* (table (alter*) "users") :bad_users)
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
  (is (= (add-column* (alter*) varchar :name 255))
      {:command :alter-create-column
       :table nil
       :if-exists false
       :columns [{:col-name "name" :type "varchar(255)" :options []}]})
  (is (= (add-column* (alter*) "name")
         {:command :alter-create-column
          :table nil
          :if-exists false
          :columns [{:col-name "name"}]})))

(deftest create-test
  (is (= (create-table :users
                       (serial :id :primary-key)
                       (varchar :name 255)
                       (varchar :email 255))
         '(0)))
  (drop-users))

(deftest create-if-not-exists-test
  (is (= (if-not-exists
           (create-table :users
                         (serial :id :primary-key)
                         (varchar :name 255)
                         (varchar :email 255)))
         '(0)))
  (drop-users))

(deftest drop-test
  (create-users)
  (is (= (drop-table :users)
         '(0))))

(deftest drop-if-exists-test
  (create-users)
  (is (= (if-exists (drop-table :users))
         '(0))))

(deftest refer-to-test
  (is (= (first (:columns (refer-to (create*) :users "varchar(255)")))
         {:col-name "user_id" 
          :type "varchar(255)" 
          :options ["REFERENCES \"users\" (\"id\")" :on-delete :set-null]}))
  (is (= (first (:columns (refer-to (create*) :users)))
         {:col-name "user_id" 
          :type "integer" 
          :options ["REFERENCES \"users\" (\"id\")" :on-delete :set-null]}))
  (is (= (first (:columns (refer-to (create*) :users :integer :test)))
         {:col-name "user_id"
          :type "integer"
          :options ["REFERENCES \"test\".\"users\" (\"id\")" :on-delete :set-null]})))

(deftest alter-test
  (create-users)
  (is (= (alter-table :users :rename-to :lusers))
      '(0))
  (drop-table :lusers)
  (create-users)
  (is (= (alter-table :users (rename :id :to :uid)))
      '(0))
  (drop-users)
  (create-users)
  (is (= (alter-table :users (add-columns (varchar :name 255 :not-null))))
      '(0))
  (is (= (alter-table :users (drop-column :name)))
      '(0))
  (drop-users)
  (create-users)
  (is (= (alter-table :users 
                      (add-columns
                        (varchar :name 255 :not-null)
                        (integer :lucky_no :unique)
                        (timestamp :create-tabled_at :default "now()")))
         '(0 0 0 0 0)))
  (drop-users))

(deftest timestamps-test
  (is (= (timestamps (table (create*) :users))
         {:command :create-table 
          :table "users" 
          :if-exists false 
          :columns [{:col-name "created_at" :type "timestamp" :options [:default "now()"]}
                    {:col-name "updated_at" :type "timestamp" :options []}]})))

(deftest schema-test
 (is (= (schema (create*) :test)
         {:command :create-table
          :if-exists false
          :table nil
          :schema "test"
          :columns []})))

(deftest create-schema*-test
   (is (= (:command (create-schema* :test))
         :create-schema)))

(deftest drop-schema-test
  (is (= (:command (drop-schema :test))
         :drop-schema)))

(deftest in-schema-test
  (-> (drop-schema :if-exists :cascade)
      (schema :test)
      execute)
  (-> (create-schema*)
      (schema :test)
      execute)
  (is (= (in-schema :test 
                    (create-table :users 
                                  (serial :id :primary-key)
                                  (integer :name :not-null))
                    (create-table :ascot
                                  (serial :id :primary-key)
                                  (timestamps))
                    (alter-table :users
                                 (add-columns (varchar :email 255 :not-null))))
         '(0 0 0 0 0)))
  (is (= (in-schema :test (drop-table :ascot))
         '(0))))

(deftest create-schema-test
  (-> (drop-schema :if-exists :cascade)
      (schema :test)
      execute)
  (is (= (create-schema :test 
                    (create-table :users 
                                  (serial :id :primary-key)
                                  (integer :name :not-null))
                    (create-table :ascot
                                  (serial :id :primary-key)
                                  (timestamps))
                    (alter-table :users
                                 (add-columns (varchar :email 255 :not-null))))
         '(0 0 0 0 0 0))))
