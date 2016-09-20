(ns thdr.avro-repo.core-test
  (:require [thdr.avro-repo
             [core :refer [load-schemas!]]]
            [clojure.test :refer :all]
            [clojure.java.io :as io]
            [boot.pod :as pod])
  (:import [org.apache.avro Schema]
           [org.apache.avro.generic GenericData$Record]))

(pod/add-classpath (io/resource "test_jar_schemas.jar"))

(deftest load-schemas-from-files-test
  (let [schemas (load-schemas! "schemas/")]
    (is (instance? Schema (:user-events schemas)))))

(deftest load-schema-from-jar-test
  (let [schemas (load-schemas! "jar_schemas/")]
    (is (instance? Schema (:user-events schemas)))))

(deftest load-schemas-from-files-key-fn
  (let [schemas (load-schemas! "schemas/"
                                 {:key-fn (fn [_ schema] (.getName schema))})]
    (is (instance? Schema (get schemas "UserEvent")))))

(deftest load-schemas-from-jar-key-fn
  (let [schemas (load-schemas! "jar_schemas/"
                                 {:key-fn (fn [_ schema] (.getName schema))})]
    (is (instance? Schema (get schemas "UserEvent")))))

(deftest nonexistent-resource
  (is (thrown-with-msg? Exception
                        #"Couldn't load"
                        (load-schemas! "some_dir/"))))
