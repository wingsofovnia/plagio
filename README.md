# Plagio

Plagio is a simple implementation of Shingles Algorithm for searching near duplicate documents **Apache Spark** engine and can be easily extended with your custom implementation.

Plagio was developed for my thesis *Parallel and Distributed Algorithms for Large Text Datasets Analysis* as a demonstration of using distrubuted processing solutions, such as Apache Spark, for distributed textual data processing.

Program available on "as is" basis and created only for education purposes.

### Requirements
  - Java Development Kit 1.8
  - Apache Spark 1.3.1 (if you want calculate document similarity with SparkCoreProcessor)

### Usage

First of all, you should build Plagio application with `eu.ioservices.plagio.Plagio`:

```java
# Run application locally on 4 cores
Plagio pl = 
    Plagio.builder()
       .config(new FileBasedConfig() {{                          // (mandatory) define configuration class
          setInputPath("Folder/with/text/files);                 // path to files to be checked
          // ...
       }})
       .core(new SimpleCoreProcessor())                          // (mandatory) determine which CoreProcessor will be used for processing data
       .stringProcessorManager(new StringProcessorManager() {{   // (optional) you may define StringProcessorManager for processing text, e.g. with
          addProcessor(new NormalizingStringProcessor());        // NormalizingStringProcessor, that cleans text from unnecessary spaces,  
       }})                                                       //   special characters with text transliterating
       .converter(new TikaConverter())                           // (optional) specify InputStream2text Converter implementation
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
 Result{docName='test-file.txt',    docShingles=160, coincidences=92,  duplicationLevel=57.49999999999999}
 Result{docName='test-file1_2.txt', docShingles=170, coincidences=165, duplicationLevel=97.05882352941177}
 Result{docName='test-file2.txt',   docShingles=200, coincidences=73,  duplicationLevel=36.5}
 */
```

## License
MIT