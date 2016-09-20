(set-env! :resource-paths #{"src"}
          :dependencies '[[org.apache.avro/avro "1.8.1"]
                          [camel-snake-kebab "0.4.0"]
                          [commons-io/commons-io "2.5"]
                          [adzerk/bootlaces "0.1.13" :scope "test"]
                          [adzerk/boot-test "1.1.1" :scope "test"]])

(require '[adzerk.boot-test :as test]
         '[adzerk.bootlaces :refer :all]
         '[clojure.java.io :as io])

(def +version+ "0.1.1-SNAPSHOT")
(bootlaces! +version+ :dont-modify-paths? true)

(task-options!
 pom {:project 'io.thdr/avro-repo
      :version +version+
      :description "Avro-schemas repository for Clojure"
      :url "https://github.com/konukhov/avro-repo"
      :scm {:url "https://github.com/konukhov/avro-repo"}
      :license {"Eclipse Public License"
                "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask test []
  (merge-env! :resource-paths #{"test_resources"}
              :source-paths #{"test"})
  (test/test))
