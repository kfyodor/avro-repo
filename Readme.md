# Avro-repository for Clojure [![CircleCI](https://circleci.com/gh/konukhov/avro-repo.svg?style=shield)](https://circleci.com/gh/konukhov/avro-repo)

A little library for managing Avro schemas in your Clojure projects.

This is a part of [thdr/kfk](https://github.com/konukhov/kfk) project: clients and helpers for working with Apache Kafka in Clojure.

## Usage

### Loading schemas.

Make sure you schemas directory is accessible somewhere on the classpath. 

Use `(load-schemas! schemas-path)` to load Avro schemas into your app. `load-schemas!` returns a map where the key by default is a kebab-cased and keywordized schema's filename and the value is an `org.apache.avro.Schema` instance.

You can provide `:key-fn` option to customize keys in a resulting map:

```clojure
(load-schemas! schemas-path 
               {:key-fn (fn [basename schema] (.getName schema))})
```

### Sharing custom types and schemas

If `types` or `includes` directories are present on provided `schemas-path`, they will be loaded first. `Avro-repo` loads schemas in the following order:

```
:schemas-path:/types/**/*.avsc
:schemas-path:/includes/**/*.avsc
everithing else on :schemas-path:
```

### Using schemas

A map with schemas can be accessed directly though it's better to create a record with `proto/SchemaRepo` protocol implemented and use it with something like [Component](https://github.com/stuartsierra/component) library.

Note that values are raw `Schema` instances and JSON-serialization is quite expensive. If you need each Schema to be serialized to JSON (for example, when working with Confluent's `schema-registry`), be sure to implement some kind of caching mechanism. In future versions I'm thinking of adding this feature to this library.

```clojure
(ns schema-repo
  (:require [com.stuartsierra.component :as component]
            [thdr.avro-repo
			  [core :refer [load-schemas!]]
			  [proto :as proto]]))
		     
(defrecord Schemas [schemas]
  component/Lifecycle
  
  (start [this]
	(assoc this :schemas (load-schemas!)))

  (stop [this]
    (assoc this :schemas {}))
	
  proto/SchemaRepo
  
  (get-schema [this k]
    (get schemas k)))
```

## Contributing

Feel free to open an issue or PR.

## License

Copyright © 2016 Theodore Konukhov <me@thdr.io>

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

