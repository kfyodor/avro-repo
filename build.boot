(set-env! :resource-paths #{"src"}
          :dependencies '[[io.thdr/kfk.avro-bridge "0.1.0-SNAPSHOT"]
                          [camel-snake-kebab "0.4.0"]
                          [adzerk/bootlaces "0.1.13" :scope "test"]
                          [adzerk/boot-test "1.1.1" :scope "test"]])

(require '[adzerk.boot-test :as test]
         '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.0-SNAPSHOT")
(bootlaces! +version+ :dont-modify-paths? true)

(task-options!
 pom {:project 'io.thdr/kfk.schema-helpers
      :version +version+
      :description "Some helpers to make work with Avro Schemas and Kafka easier"
      :url "https://github.com/konukhov/kfk-schema-helpers"
      :scm {:url "https://github.com/konukhov/kfk-schema-helpers"}
      :license {"Eclipse Public License"
                "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask test []
  (merge-env! :resource-paths #{"test_resources"}
              :source-paths #{"test"})
  (test/test))
