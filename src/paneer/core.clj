(ns paneer.core
  (:refer-clojure :exclude [bigint boolean char double float time]))

(defn- command
  [command {:keys [if-exists]}]
  {:command command
   :if-exists (or if-exists false)
   :table nil
   :columns []})

(defn create*
  "Starts a CREATE TABLE command"
  [& opts]
  (command :create opts)) 

(defn alter*
  "Starts an ALTER TABLE command"
  [& opts]
  (command :alter opts))

(defn drop*
  "Starts a DROP TABLE command"
  [& opts]
  (command :drop opts))

(defn table*
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
  (when-not (= (:command command) :alter)
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
    (apply column command col-options)))

;; Helper Functions for Creating specifically typed columns

(defn integer
  "Creates an Integer Column"
  [command col-name & options]
  (column command col-name :integer options))

(defn varchar
  "Creates a Varchar Column"
  [command col-name length & options]
  (column command col-name (str "varchar(" length ")") options))

(defn float
  "Creates a Float Column"
  [command col-name & options]
  (column command col-name :float options))

(defn timestamp
  "Creates a timestamp column"
  [command col-name & options]
  (column command col-name :timestamp options))

(defn serial
  "Creates a serial column"
  [command col-name & options]
  (column command col-name :serial options))

(defn text
  "Creates a text column"
  [command col-name & options]
  (column command col-name :serial options))

(defn bigint
  "Creates a bigint column"
  [command col-name & options]
  (column command col-name :bigint options))

(defn boolean
  "Creates a boolean column"
  [command col-name & options]
  (column command col-name :boolean options))

(defn double
  "Creates a double precision column"
  [command col-name & options]
  (column command col-name "double precision" options))


