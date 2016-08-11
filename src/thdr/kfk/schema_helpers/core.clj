(ns thdr.kfk.schema-helpers.core
  (:require [thdr.kfk.schema-helpers.proto :as proto]
            [clojure.java.io :as io]
            [thdr.kfk.avro-bridge.core :as avro]
            [camel-snake-kebab.core :refer [->kebab-case ->snake_case]])
  (:import [org.apache.avro Schema]))

(defn load-schemas!
  "Loads Avro schemas from resources into a map."
  ([] (load-schemas! "avro/"))
  ([path]
   (loop [files (file-seq (io/file (io/resource path))) schemas {}]
     (if-let [file (first files)]
       (if-let [[_ name] (re-matches #"\A(.+)\.avsc\z" (.getName file))]
         (let [schema (Schema/parse (slurp file))
               key (-> name ->kebab-case keyword)]
           (recur (rest files) (assoc schemas key schema)))
         (recur (rest files) schemas))
       schemas))))

(defmacro defkafkamessage
  "A helper macro which generates a function make-:event:-message.
   Sets the topic same to `event` name, transforms a value with
   provided `serialize-fn`, gets a schema from provided schema repo
   (must implement a Schema protocol with `get-schema` by topic name)
   and finally creates an Avro object and puts it in a `:value` key.

   Example:

   First of all, you should  have your avro schemas in resources/avro dir.
   Schema name must match a Kafka topic name which this message will be
   sent to.

   (defrecord Schemas ;; it's better to use this with something like Component
     SchemaRepo
     (get-schema [this k]
       (get (:schemas this) k)))

   (def schema-repo
     (map->Schemas {:schemas (load-schemas! \"your_schemas_path\")}))

   (defkafkamessage user-created
     :topic :user-events ;; a Kafka topic message will be sent to
     :key-fn #(-> % :id str) ;; applied to obj before serialization
     :serialize-fn #(merge % {:type :created :id (uuid-to-bytes (:id %))}))

   (make-user-created-message schema-repo
                              {:id (java.util.UUID/randomUUID)}
                              {:partition 0}) ;; optional Kafka message keys"
  [event & {:keys [topic key-fn serialize-fn deserialize-fn]
            :or {serialize-fn identity
                 deserialize-fn identity}}]
  {:pre [(not (nil? topic))]}
  (let [fn-name (symbol (str "make-" event "-message"))]
    `(defn ~fn-name
       ([schemas# obj#]
        (~fn-name schemas# obj# {}))
       ([schemas# obj# opts#]
        (let [{partition# :partition
               key# :key
               timestamp# :timestamp} opts#
              sch# (proto/get-schema schemas# ~topic)]
          {:topic ~(-> topic name ->snake_case)
           :partition partition#
           :timestamp timestamp#
           :key (cond key# key# ~key-fn (~key-fn obj#) :else nil)
           :value (avro/->java sch# (~serialize-fn obj#))})))))
