(ns paneer.db-test
  (:use clojure.test
        paneer.db))

(deftest db-spec-from-uri-test
  (is (= (db-spec-from-uri "postgres://asdfqwer98uafsd:qwerasdf@example.io:4311/example")
         {:classname "org.postgresql.Driver"
          :subprotocol "postgresql"
          :user "asdfqwer98uafsd"
          :password "qwerasdf"
          :subname "//example.io:4311/example"}))
  (is (= (db-spec-from-uri "h2://user/db")
         {:classname "org.h2.Driver"
          :subprotocol "h2"
          :subname "//user/db"})))
