(ns thdr.kfk.schema-helpers.core-test
  (:require [thdr.kfk.schema-helpers
             [core :as h]
             [proto :as p]]
            [clojure.test :refer :all]
            [clojure.java.io :as io]
            [boot.pod :as pod])
  (:import [org.apache.avro Schema]
           [org.apache.avro.generic GenericData$Record]))

(pod/add-classpath (io/resource "test_jar_schemas.jar"))

(deftest load-schemas-from-files-test
  (let [schemas (h/load-schemas! "schemas/")]
    (is (instance? Schema (:user-events schemas)))))

(deftest load-schema-from-jar-test
  (let [schemas (h/load-schemas! "jar_schemas/")]
    (is (instance? Schema (:user-events schemas)))))

(defrecord Schemas [schemas]
  p/SchemaRepo

  (get-schema [this k]
    (get schemas k)))

(deftest apply-schema-test
  (let [schemas (->Schemas (h/load-schemas! "schemas/"))
        msg {:topic "user_events"
             :value {:id (->> (java.util.UUID/randomUUID)
                              (str)
                              (map byte)
                              (byte-array))}}]
    (is (instance? GenericData$Record
                   (:value (h/apply-schema schemas msg))))))
