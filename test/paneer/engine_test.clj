(ns paneer.engine-test
  (:use clojure.test
        paneer.engine))

(deftest make-query-test
  (is (= (make-query 
           {:command :create
            :if-exists false
            :table "users"
            :columns [{:type "serial" :col-name "id" :options [:primary-key :not-null]}
                      {:type "varchar(255)" :col-name "name" :options [:not-null]}
                      {:type "varchar(255)":col-name "email" :options [:not-null]}]})
         "CREATE TABLE \"users\" (\"id\" serial PRIMARY KEY NOT NULL, \"name\" varchar(255) NOT NULL, \"email\" varchar(255) NOT NULL);"))
  (is (= (make-query 
           {:command :create
            :if-exists false
            :table "things"
            :columns [{:type "serial" :col-name "id" :options [:primary-key :not-null]}
                      {:type "integer":col-name "user_id" :options ["REFERENCE \"users\" (\"id\")" :on-delete :set-null]}
                      {:type "timestamp" :col-name "created_at" :options [:default "now()"]}]})
         "CREATE TABLE \"things\" (\"id\" serial PRIMARY KEY NOT NULL, \"user_id\" integer REFERENCE \"users\" (\"id\") ON DELETE SET NULL, \"created_at\" timestamp DEFAULT now());"))
  (is (= (make-query
           {:command :alter-rename
            :if-exists true
            :table "users"
            :new-table "bad_users"})
         "ALTER TABLE IF EXISTS \"users\" RENAME TO \"bad_users\";"))
  (is (= (make-query
           {:command :alter-create-column
            :if-exists false
            :table "users"
            :columns [{:type "integer" :col-name "lucky-no" :options [:unique]}]})
         "ALTER TABLE \"users\" ADD COLUMN \"lucky-no\" integer UNIQUE;"))
  (is (= (make-query
           {:command :drop
            :if-exists true
            :table "users"})
         "DROP TABLE IF EXISTS \"users\";"))
  (is (= (make-query
           {:command :alter-drop-column
            :if-exists true 
            :table "users"
            :columns [{:col-name "name"}]})
         "ALTER TABLE IF EXISTS \"users\" DROP COLUMN IF EXISTS \"name\";"))
  (is (= (make-query
           {:command :alter-rename-column
            :if-exists false
            :table "users"
            :columns [{:col-name "name"} {:col-name "user-name"}]})
         "ALTER TABLE \"users\" RENAME COLUMN \"name\" TO \"user-name\";")))
