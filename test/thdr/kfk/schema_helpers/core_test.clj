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

(h/defkafkamessage user-created
  :topic :user-events
  :key-fn #(-> % :id str)
  :serialize-fn
  (fn [{:keys [id]}]
    {:id (->> id str (map byte) byte-array)
     :event-type :created}))

(h/defkafkamessage user-updated
  :topic :user-events
  :key-fn #(-> % :id str)
  :serialize-fn
  (fn [{:keys [id]}]
    {:id (->> id str (map byte) byte-array)
     :event-type :updated}))

(deftest macro-test
  (let [schema-repo (map->Schemas {:schemas (h/load-schemas!)})
        uuid        (java.util.UUID/randomUUID)
        user        {:id uuid}
        {ckey :key
         cvalue :value
         ctopic :topic} (make-user-created-message schema-repo user)
        {ukey :key
         uvalue :value
         utopic :topic} (make-user-updated-message schema-repo user)]
    (is (= (str uuid) ukey ckey))
    (is (instance? GenericData$Record uvalue))
    (is (instance? GenericData$Record cvalue))))
