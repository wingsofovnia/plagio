# Plagio

Plagio is a simple implementation of Shingles Algorithm for searching near duplicate documents using **Apache Spark** engine.

Program was developed for my thesis *Parallel and Distributed Algorithms for Large Text Datasets Analysis* as a demonstration of using distrubuted processing solutions, such as Apache Spark, for distributed textual data processing.

Program available on "as is" basis and created only for education purposes.

### Requirements
  - Java Development Kit 1.8
  - Apache Spark 1.3.1
  - Any database supported by JDBC

### Usage

You need Apache Spark 1.3.1 and Java 1.8 installed for running **Plagio** smoothly.

As any Apache Spark application (driver app) you can run it locally:

```sh
# Run application locally on 4 cores
./bin/spark-submit \
  --class eu.ioservices.plagio.EntryPoint \
  --master local[4] \
  /target/eu.ioservices.plagio-jar-with-dependencies.jar \
  config.properties // optional
```

or run it on cluster:

```sh
# Run on a Spark standalone cluster
./bin/spark-submit \
  --class eu.ioservices.plagio.EntryPoint \
  --master spark://myspark:7077 \
  /target/eu.ioservices.plagio-jar-with-dependencies.jar
```

The typical output looks like:
```
#-------------------------------------------#
  -> Document #1 (Egzamin-answer-1.docx)
     Coincides: 150
     PLAGIARISM LEVEL: 89%

  -> Document #2 (Egzamin-answer-Part.docx)
     Coincides: 63
     PLAGIARISM LEVEL: 100%

  -> Document #3 (Tematy egzaminacyjne 2015.docx)
     Coincides: 165
     PLAGIARISM LEVEL: 20%
#-------------------------------------------#
```

## License
MIT