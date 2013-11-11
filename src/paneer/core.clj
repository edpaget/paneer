(ns paneer.core
  (:require [clojure.java.jdbc :as j]
            [paneer.db :refer :all])
  (:refer-clojure :exclude [bigint boolean char double float]))

(defn- command
  [command & [if-exists]]
  {:command command
   :if-exists (or if-exists false)
   :table nil
   :columns []})

(defn create*
  "Starts a CREATE TABLE command"
  [& [if-exists]]
  (command :create-table (= :if-exists if-exists))) 

(defn alter*
  "Starts an ALTER TABLE command"
  [& [if-exists]]
  (command :alter-table (= :if-exists if-exists)))

(defn drop*
  "Starts a DROP TABLE command"
  [& [if-exists]]
  (command :drop-table (= :if-exists if-exists)))

(defn transaction
  "Groups multiple commands togeter into a single transaction"
  [& commands]
  (assoc (command :transaction) :commands commands))

(defn schema
  "Adds a schema qualification to existing command"
  [command schema-name]
  (assoc command :schema (name schema-name)))

(defn create-schema*
  "Starts a new create-schema command"
  [& [if-exists]]
  (command :create-schema (= :if-exists if-exists)))

(defn drop-schema
  "Starts a drop-schema command"
  [& [if-exists cascade]]
  (-> (command :drop-schema (= :if-exists if-exists))
      (assoc :cascade (= :cascade cascade))))

(defn table
  "Modifyes a command map to include table name"
  [command table-name]
  (merge command {:table (name table-name)}))

(defn column
  "Adds a column to a command map"
  [command col-name & [col-type & options]]
  (let [column-def {:col-name (name col-name)} 
        column-def (merge column-def (when col-type 
                                       {:type (name col-type) 
                                        :options (into [] options)}))]
    (update-in command [:columns] conj column-def)))

(defn- must-be-alter
  [command]
  (when-not (= (:command command) :alter-table)
    (throw (Exception. "Command Must be :alter"))))

(defn drop-column*
  "Specifies column to drop in ALTER TABLE command"
  [command col-name]
  (must-be-alter command)
  (column (update-in command [:command] (constantly :alter-drop-column)) col-name))

(defn rename-to*
  "Renames table as part of ALTER TABLE command"
  [command new-table]
  (must-be-alter command)
  (when-not (:table command)
    (throw (Exception. "Must Define Table to Rename")))
  (merge command {:new-table (name new-table) :command :alter-rename}))

(defn rename-column-to*
  "Renames a column as part of ALTER TABLE command"
  [command col-name]
  (must-be-alter command)
  (when (empty? (:columns command))
    (throw (Exception. "Must provide a target column to rename")))
  (column (update-in command [:command] (constantly :alter-rename-column)) col-name))

(defn add-column*
  "Adds a column as part of a ALTER TABLE Command"
  [command & col-options]
  (must-be-alter command)
  (let [command (update-in command [:command] (constantly :alter-create-column))] 
    (cond (nil? col-options) command
          (fn? (first col-options)) (apply (first col-options) command (rest col-options))
          true (apply column command col-options))))

(defmacro if-exists
  "Transforms command into the if exists version"
  [command]
  (let [[_ [command] & body] (macroexpand-1 command)
        command (list command :if-exists)]
    `(-> ~command
         ~@body)))

(defmacro if-not-exists 
  [command]
  `(if-exists ~command))

(defmacro in-schema
  [schema-name & commands]
  (let [commands (map (comp drop-last macroexpand-1) commands)]
    (if (> (count commands) 1)
      `(->> (map #(schema % ~schema-name) (list ~@commands)) 
            (apply transaction)
            execute)
      `(-> ~@commands
           (schema ~schema-name)
           execute))))

(defmacro create-schema
  [schema-name & commands]
  `(in-schema ~schema-name (create-schema* ~schema-name) ~@commands))

(defmacro create-table
  "Allows you to wrap a table definition together as in
  (create-table :users
           (serial :id :primary-key)
           (varchar :name 255)
           (varchar :email 255))
  then automatically executes it against the current default database."
  [tbl-name & columns]
  `(-> (create*)
       (table  ~tbl-name)
       ~@columns
       execute))

(defmacro drop-table
  "Wrapper for dropping tables allows you to write:
  (drop-table :users)
  then automatically executes it against the current default database. "
  [tbl-name]
  `(-> (drop*)
       (table ~tbl-name)
       execute))

(defmacro rename
  "Internally used by alter macro"
  [command column _ new-name]
  `(-> ~command
       (column ~column)
       (rename-column-to* ~new-name)))

(defmacro add-columns
  "Internally used by alter macro"
  [command & columns]
  `(-> ~command
       (add-column*)
       ~@columns))

(defmacro drop-column
  "Internally used by alter macro"
  [command column-name]
  `(drop-column* ~command ~column-name))

(defmacro alter-table
  "Wrapper for altering tables. Allows you to write:
  (alter-table :users :rename-to :lusers)

  (alter-table :users 
               (add-columns (varchar :api-key 255 :not-null)))

  (alter-table :users 
               (rename :email :to :snailmail))

  (alter-table :users
               (drop-column :email))"

  ([tbl-name _ new-name]
   `(-> (alter*)
        (table ~tbl-name)
        (rename-to* ~new-name)
        execute))
  ([tbl-name [action & args]]
   `(-> (alter*)
        (table ~tbl-name)
        (~action ~@args)
        execute)))

;; Helper Functions for Creating specifically typed columns

(defn integer
  "Creates an Integer Column"
  [command col-name & options]
  (apply column command col-name :integer options))

(defn varchar
  "Creates a Varchar Column"
  [command col-name length & options]
  (apply column command col-name (str "varchar(" length ")") options))

(defn float
  "Creates a Float Column"
  [command col-name & options]
  (apply column command col-name :float options))

(defn timestamp
  "Creates a timestamp column"
  [command col-name & options]
  (apply column command col-name :timestamp options))

(defn serial
  "Creates a serial column"
  [command col-name & options]
  (apply column command col-name :serial options))

(defn text
  "Creates a text column"
  [command col-name & options]
  (apply column command col-name :serial options))

(defn bigint
  "Creates a bigint column"
  [command col-name & options]
  (apply column command col-name :bigint options))

(defn boolean
  "Creates a boolean column"
  [command col-name & options]
  (apply column command col-name :boolean options))

(defn double
  "Creates a double precision column"
  [command col-name & options]
  (apply column command col-name "double precision" options))

(defn refer-to
  "Creates a reference column"
  [command table & [type schema]]
  (let [cname (str (->> table name butlast (apply str)) "_id")
        options [(str "REFERENCES \"" (when schema (str (name schema) "\".\"")) (name table) "\" (\"id\")") :on-delete :set-null]]
    (apply column command cname (or type :integer) options)))

(defn timestamps
  "Create a updated_at and created_at column"
  [command]
  (-> (timestamp command :created_at :default "now()")
      (timestamp :updated_at)))
