# Plagio

Plagio is a simple implementation of Shingles Algorithm for searching near duplicate documents **Apache Spark** engine.

Plagio was developed for my thesis *Parallel and Distributed Algorithms for Large Text Datasets Analysis* as a demonstration of using distributed processing solutions, such as Apache Spark, for distributed textual data processing.

Program available on "as is" basis and created only for education purposes.

### Requirements
  - Java Development Kit 1.8
  - Apache Spark 1.5.2

### Usage

First of all, you should build Plagio application with `eu.ioservices.plagio.Plagio`:

```java
Plagio pl = 
    Plagio.builder()
          .config(new Configuration("config.properties"))            // (mandatory) Plagio configuration instance
          .textProcessorManager(new TextProcessorManager() {{        // (optional) you may define Text ProcessorManager for processing text, e.g. with
             addProcessor(new NormalizingTextProcessor());           //   NormalizingStringProcessor, that cleans text from unnecessary spaces
          }})                                                        //   and special characters with text transliterating
          .textConverter(new TikaConverter())                        // (optional) specify InputStream2text Converter implementation
          .documentShinglesSupplier(new TextShinglesSupplier())      // (mandatory) supplier of new shingles to be checked
          .cachedShinglesSupplier(new CachedTextShinglesSupplier())  // (mandatory) supplier of old, cached shingles
          .shinglesCacheProducer(new ObjectCacheProducer())          // (mandatory) cache producer, that saves new, unknown shingles (shingles, that aren't neither found in cache nor intersect with shingles, from other new documents.
          .build()
```

and execute it:

```java
List<Result> results = pl.process();
```

It returns a list of `eu.ioservices.plagio.model.Result` objects, that encapsulates resulting data produced by Plagio
and contains information about documents and its duplication level:

```java
results.stream().map(Result::toString).forEach(System.out::println);
/*  --> STDOUT output:
 Result{docName='test-file.txt',    docShingles=160, docCoincidences=92,  duplicationLevel=57.49999999999999}
 Result{docName='test-file1_2.txt', docShingles=170, docCoincidences=165, duplicationLevel=97.05882352941177}
 Result{docName='test-file2.txt',   docShingles=200, docCoincidences=73,  duplicationLevel=36.5}
 */
```

## License
MIT
