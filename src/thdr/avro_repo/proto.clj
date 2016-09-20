(ns thdr.avro-repo.proto)

(defprotocol SchemaRepo
  (get-schema [this k])
  (get-schema-json [this k])
  (apply-schema [this k v]))
