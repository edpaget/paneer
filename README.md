# paneer [![Build Status](https://travis-ci.org/edpaget/paneer.png?branch=master)](https://travis-ci.org/edpaget/paneer)

A Clojure Library to describe and alter tables. Think of it as complimentary to the features offered in [Korma](https://github.com/korma/Korma). It can also be used with [drift](https://github.com/macourtney/drift) to provide a nice solution for doing SQL migrations in Clojure.  

Get the current version from Clojars with:

      [paneer "0.2.0"]

## Usage

Paneer is a tool for creating, altering, and dropping tables in a SQL database. See the [Docs](https://edpaget.github.io/paneer/) for a full description of the API.  

A Few Simple Examples:

    (ns some.namespace
      (:refer paneer.core :all))

    (create-table :users
       (serial :id :primary-key)
       (varchar :name 255 :not-null)
       (varchar :email 255 :not-null)
       (timestamps))

    (alter-table :users
        (rename (column :name) (to :user-name))))

    (drop-table :users))

Or if you prefer a chained style

    (-> (create*)
        (table* :users)
        (serial :id :primary-key)
        (varchar :name 255 :not-null)
        (varchar :email 255 :not-null))

Which will not be evaluated until you pass the command as an argument to `(execute)` (or `with-connection` or `sql-string`). 

Paneer can reuse Korma's db connection if it is present. Make sure to include `paneer.korma` and the top of any ns you want to use this feature in, then run any queries with `(exec-korma command-ap)` or by invoking command maps with `(-> ..rest of command.. (exec-korma*))`, and they'll be run against Korma's current db connection. Support for using multiple Korma connections is in the pipeline.

Otherwise you can pass either a a Heroku-style DATABASE_URL uri to Paneer or a [jdbc-style](https://github.com/clojure/java.jdbc) database connection map to Paneer to execute the sql. 

    (with-connection "postgres://user:pass@host:port/db-name" (create (table :users)))

or wrap a command as 
  
    (sql-string (create (table :users))) 

to returnt the sql string. 

## License

Copyright © 2013 Edward Paget 

Distributed under the GNU General Public License (GPL). See COPYING.
