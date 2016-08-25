(ns thdr.kfk.schema-helpers.core
  (:require [thdr.kfk.schema-helpers.proto :as proto]
            [clojure.java.io :as io]
            [thdr.kfk.avro-bridge.core :as avro]
            [camel-snake-kebab.core :refer [->kebab-case ->snake_case]]
            [clojure.string :as str])
  (:import [org.apache.avro Schema]
           [java.io File]
           [java.util.jar JarFile JarFile$JarFileEntry]))

(defmulti schema-seq
  (fn [path-name]
    (keyword (.getProtocol (io/resource path-name)))))

(defmethod schema-seq :file
  [path-name]
  (file-seq (io/file (io/resource path-name))))

(defmethod schema-seq :jar
  [path-name]
  (let [abs-path (-> (.getPath (io/resource path-name))
                     (str/replace #"\Afile\:((.+?)\.jar)\!.+\z" "$1"))]
    (iterator-seq (.entries (JarFile. abs-path)))))

(defmulti maybe-get-schema-file
  (fn [path-name file]
    (type file)))

(defmethod maybe-get-schema-file File
  [path-name file]
  (when-let [[_ schema-name] (re-matches #"\A(.+)\.avsc\z" (.getName file))]
    [schema-name file]))

(defmethod maybe-get-schema-file JarFile$JarFileEntry ;; check on different jdks
  [path-name file]
  (let [pattern (re-pattern (str "\\A" path-name "(.+?)\\.avsc\\z"))
        file-path (.getName file)
        [_ schema-name] (re-matches pattern file-path)]
    (when schema-name
      [schema-name (io/resource file-path)])))

(defn load-schemas!
  "Loads Avro schemas from resources (either local resources or
   resources from a dependent .jar file) into a map.
   TODO: support for nested dirs???"
  ([] (load-schemas! "schemas/"))
  ([path]
   (reduce (fn [schemas file]
             (if-let [[schema-name file] (maybe-get-schema-file path file)]
               (assoc schemas
                      (-> schema-name ->kebab-case keyword)
                      (Schema/parse (slurp file)))
               schemas))
           {}
           (schema-seq path))))

(defn apply-schema
  [schema-repo {:keys [topic] :as msg}]
  (let [schema-name (-> topic keyword ->kebab-case)
        schema (proto/get-schema schema-repo schema-name)]
    (update-in msg [:value] (partial avro/->java schema))))
