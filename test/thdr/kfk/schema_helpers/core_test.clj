(ns thdr.kfk.schema-helpers.core-test
  (:require [thdr.kfk.schema-helpers
             [core :as h]
             [proto :as p]]
            [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:import [org.apache.avro Schema]
           [org.apache.avro.generic GenericData$Record]))

(deftest load-schemas-test
  (let [schemas (h/load-schemas! "avro")]
    (is (instance? Schema (:user-events schemas)))))

(defrecord Schemas [schemas]
  p/SchemaRepo

  (get-schema [this k]
    (get schemas k)))

(deftest apply-schema-test
  (let [schemas (->Schemas (h/load-schemas! "avro"))
        msg {:topic "user_events"
             :value {:id (->> (java.util.UUID/randomUUID)
                              (str)
                              (map byte)
                              (byte-array))}}]
    (is (instance? GenericData$Record
                   (:value (h/apply-schema schemas msg))))))
