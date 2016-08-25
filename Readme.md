# Schema and Kafka helpers for Clojure [![CircleCI](https://circleci.com/gh/konukhov/kfk-schema-helpers.svg?style=shield)](https://circleci.com/gh/konukhov/kfk-schema-helpers)

Some helpers for managing Avro schemas in your projects + using those schemas to de/serialize Kafka Messages.

This is a part of `kfk` project.

_In progress_

## Usage

### Loading schemas.

Put your schemas somewhere into `resources` dir.

Use `(load-schemas! schemas-path)` to load Avro schemas into your app. `load-schemas!` returns a map where the key is a kebab-cased and keywordized name of a schema and the value is an `org.apache.avro.Schema` instance.

You can access this map directly or it's even better to create a record with `proto/SchemaRepo` protocol implemented and use it with something like [Component](https://github.com/stuartsierra/component) library.

```
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

