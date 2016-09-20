(ns thdr.avro-repo.core
  (:require [clojure.java.io :as io]
            [camel-snake-kebab.core :refer [->kebab-case]]
            [clojure.string :as str])
  (:import [org.apache.avro Schema Schema$Parser]
           [org.apache.commons.io FilenameUtils FileUtils IOCase]
           [java.io File]
           [java.util.jar JarFile JarFile$JarFileEntry]))

(defmulti get-schema-paths
  "Gets the collection of paths and all
   other necessary information about schema files in order
   to load schemas correctly."
  (fn [base-path resource] ;; there're both path and resource in order to prevent 2 calls to io/resource
    (if resource
      (keyword (.getProtocol resource))
      :nonexistent-resource)))

(defn- in-dir? [base-path subdir file-path]
  (FilenameUtils/directoryContains
   (FilenameUtils/concat base-path subdir)
   file-path))

(defn- schema-role
  "Determin whether schema is a `type`, `include` or `schema`"
  [base-path file-path]
  (cond (in-dir? base-path "includes" file-path) :includes
        (in-dir? base-path "types" file-path) :types
        :else :schemas))

(defmethod get-schema-paths :file
  [_ dir]
  (let [base-path (.getPath dir)]
    (->> (FileUtils/iterateFiles (io/file dir) (into-array ["avsc"]) true)
         (iterator-seq)
         (map (fn [file]
                (let [path (.getPath file)]
                  {:type (schema-role base-path path)
                   :basename (FilenameUtils/getBaseName path)
                   :resource file}))))))

(defmethod get-schema-paths :jar
  [base-path resource]
  (let [full-path (-> (.getPath resource)
                      (str/replace #"\Afile\:((.+?)\.jar)\!.+\z" "$1"))]
    (->> (.entries (JarFile. full-path))
         (iterator-seq)
         (reduce (fn [schemas file]
                   (let [path (.getName file)]
                     (if (FilenameUtils/isExtension path "avsc")
                       (conj schemas
                             {:type (schema-role base-path path)
                              :basename (FilenameUtils/getBaseName path)
                              :resource (io/resource path)})
                       schemas))) []))))

(defmethod get-schema-paths :nonexistent-resource
  [base-path resource]
  (throw (Exception. (format "Couldn't load `%s`. Resource not found"
                             base-path))))

(defmethod get-schema-paths :default
  [base-path resource]
  (throw (Exception.
          (format "`%s`-resources are not supported yet"
                  (.getProtocol base-path)))))

(defn- default-key-fn
  [basename schema]
  (-> basename ->kebab-case keyword))

(defn- load-schema!
  "Loads and parses an Avro schema"
  [parser {:keys [resource basename] :as schema} key-fn]
  (let [json (slurp resource)
        schema (.parse parser json)]
    [(key-fn basename schema) schema]))

(defn load-schemas!
  "Loads Avro schemas from resources (either local resources or
   resources from a .jar file on classpath) into a map."
  ([]
   (load-schemas! "schemas/" {}))
  ([base-path]
   (load-schemas! base-path {}))
  ([base-path {:keys [key-fn] :or {key-fn default-key-fn} :as opts}]
   (let [resource (io/resource base-path)
         schema-paths (->> (get-schema-paths base-path resource)
                           (group-by :type))
         parser (Schema$Parser.)]
     (apply merge (map (fn [role]
                         (->> (role schema-paths)
                              (map #(load-schema! parser % key-fn))
                              (into {})))
                       [:types :includes :schemas])))))
