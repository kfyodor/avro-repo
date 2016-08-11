(ns thdr.kfk.schema-helpers.proto)

(defprotocol SchemaRepo
  (get-schema [this k]))
