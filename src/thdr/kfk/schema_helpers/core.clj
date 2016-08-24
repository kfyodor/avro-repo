(ns thdr.kfk.schema-helpers.core
  (:require [thdr.kfk.schema-helpers.proto :as proto]
            [clojure.java.io :as io]
            [thdr.kfk.avro-bridge.core :as avro]
            [camel-snake-kebab.core :refer [->kebab-case ->snake_case]])
  (:import [org.apache.avro Schema]))

(defn load-schemas!
  "Loads Avro schemas from resources into a map."
  ([] (load-schemas! (io/resource "avro/")))
  ([^java.net.URL resource-path]
   (loop [files (file-seq (io/file resource-path)) schemas {}]
     (if-let [file (first files)]
       (if-let [[_ name] (re-matches #"\A(.+)\.avsc\z" (.getName file))]
         (let [schema (Schema/parse (slurp file))
               key (-> name ->kebab-case keyword)]
           (recur (rest files) (assoc schemas key schema)))
         (recur (rest files) schemas))
       schemas))))

(defn apply-schema
  [schema-repo {:keys [topic] :as msg}]
  (let [schema-name (-> topic keyword ->kebab-case)
        schema (proto/get-schema schema-repo schema-name)]
    (update-in msg [:value] (partial avro/->java schema))))
