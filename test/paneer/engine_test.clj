(ns paneer.engine-test
  (:use clojure.test
        paneer.engine))

(deftest eval-query-test
  (is (= (eval-query 
           {:command :create-table
            :if-exists false
            :schema nil
            :table "users"
            :columns [{:type "serial" :col-name "id" :options [:primary-key :not-null]}
                      {:type "varchar(255)" :col-name "name" :options [:not-null]}
                      {:type "varchar(255)":col-name "email" :options [:not-null]}]})
         "CREATE TABLE \"users\" (\"id\" serial PRIMARY KEY NOT NULL, \"name\" varchar(255) NOT NULL, \"email\" varchar(255) NOT NULL);"))
  (is (= (eval-query 
           {:command :create-table
            :if-exists false
            :schema nil
            :table "things"
            :columns [{:type "serial" :col-name "id" :options [:primary-key :not-null]}
                      {:type "integer":col-name "user_id" :options ["REFERENCE \"users\" (\"id\")" :on-delete :set-null]}
                      {:type "timestamp" :col-name "created_at" :options [:default "now()"]}]})
         "CREATE TABLE \"things\" (\"id\" serial PRIMARY KEY NOT NULL, \"user_id\" integer REFERENCE \"users\" (\"id\") ON DELETE SET NULL, \"created_at\" timestamp DEFAULT now());"))
  (is (= (eval-query
           {:command :alter-rename
            :if-exists true
            :schema nil
            :table "users"
            :new-table "bad_users"})
         "ALTER TABLE IF EXISTS \"users\" RENAME TO \"bad_users\";"))
  (is (= (eval-query
           {:command :alter-create-column
            :if-exists false
            :schema nil
            :table "users"
            :columns [{:type "integer" :col-name "lucky-no" :options [:unique]}]})
         "ALTER TABLE \"users\" ADD COLUMN \"lucky-no\" integer UNIQUE;"))
  (is (= (eval-query
           {:command :alter-create-column
            :if-exists false
            :schema nil
            :table "users"
            :columns [{:type "integer" :col-name "lucky-no" :options [:unique]}
                      {:type "varchar(255)" :col-name "crack-spot" :options [:not-null]}]})
         ["BEGIN;" 
          "ALTER TABLE \"users\" ADD COLUMN \"lucky-no\" integer UNIQUE;"
          "ALTER TABLE \"users\" ADD COLUMN \"crack-spot\" varchar(255) NOT NULL;"
          "COMMIT;"]))
  (is (= (eval-query
           {:command :drop-table
            :if-exists true
            :schema nil
            :table "users"})
         "DROP TABLE IF EXISTS \"users\";"))
  (is (= (eval-query
           {:command :alter-drop-column
            :if-exists true 
            :schema nil
            :table "users"
            :columns [{:col-name "name"}]})
         "ALTER TABLE IF EXISTS \"users\" DROP COLUMN IF EXISTS \"name\";"))
  (is (= (eval-query
           {:command :alter-rename-column
            :if-exists false
            :schema nil
            :table "users"
            :columns [{:col-name "name"} {:col-name "user-name"}]})
         "ALTER TABLE \"users\" RENAME COLUMN \"name\" TO \"user-name\";"))
  (is (= (eval-query
           {:command :transaction  
            :commands [{:command :create-schema
                        :schema "pubic"}

                       {:command :create-table
                        :table "users"
                        :if-exists false
                        :schema "pubic"
                        :columns [{:col-name "id" 
                                   :type "serial" 
                                   :options [:primary-key]}
                                  {:col-name "name" 
                                   :type "varchar(255)" 
                                   :options [:not-null]}]}

                       {:command :create-table
                        :table "projects"
                        :if-exists false
                        :schema "pubic"
                        :columns [{:col-name "id" 
                                   :type "serial" 
                                   :options [:primary-key]}
                                  {:col-name "name" 
                                   :type "varchar(255)" 
                                   :options [:not-null]}]}]})
         ["BEGIN;"
          "CREATE SCHEMA \"pubic\";"
          "CREATE TABLE \"pubic\".\"users\" (\"id\" serial PRIMARY KEY, \"name\" varchar(255) NOT NULL);"
          "CREATE TABLE \"pubic\".\"projects\" (\"id\" serial PRIMARY KEY, \"name\" varchar(255) NOT NULL);"
          "COMMIT;"]))
  (is (= (eval-query
           {:command :create-schema
            :schema "pubic"})
         "CREATE SCHEMA \"pubic\";"))
  (is (= (eval-query
           {:command :drop-schema
            :schema "pubic"
            :cascade false})
         "DROP SCHEMA \"pubic\";"))
  (is (= (eval-query
           {:command :drop-schema
            :schema "pubic"
            :cascade true})
         "DROP SCHEMA \"pubic\" CASCADE;")))
