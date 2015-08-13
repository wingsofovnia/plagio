# Placherk

Placherk is a simple implementation of Shingles Algorithm for searching near duplicate documents using **Apache Spark** engine.

Program was developed for my thesis *Parallel and Distributed Algorithms for Large Text Datasets Analysis* as a demonstration of using distrubuted processing solutions, such as Apache Spark, for distributed textual data processing.

Program available on "as is" basis and created only for education purposes.

### Requirements
  - Java Development Kit 1.8
  - Apache Spark 1.3.1
  - Any database supported by JDBC

### Usage

You need Apache Spark 1.3.1 and Java 1.8 installed for running **Plachert** smoothly.

As any Apache Spark application (driver app) you can run it locally:

```sh
# Run application locally on 4 cores
./bin/spark-submit \
  --class ua.edu.sumdu.dl.PlachertSparkDriver \
  --master local[4] \
  /target/ua.edu.sumdu.dl.plachert.jar \
  **args**
```

or run it on cluster:

```sh
# Run on a Spark standalone cluster
./bin/spark-submit \
  --class ua.edu.sumdu.dl.PlachertSparkDriver \
  --master spark://myspark:7077 \
  /target/ua.edu.sumdu.dl.plachert.jar \
  **args**
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

Make shure that all your nodes has jdbc driver in their classpathes for database, you have specified in configuration file.

### Configuration

Placherk supports customising config values via *property* file or startup parameters. Startup paramenters have a priority over in-file configured values and will be override in run-time.

```
Usage: PlachertSparkDriver [--debug] - enable debug logging
                           [--config] - path to config properties file
                           [--output] - results output file
                           [--silent] - disable results output
                           [--verbose] - enable spark logging
                           [--no-save] - do not save new documents' shinglesAmount into db
                           [--documents] - documents' ids as CVS for IN query (10,20...)
```             

For example:

```sh
# Run application locally on 4 cores
./bin/spark-submit \
  --class ua.edu.sumdu.dl.PlachertSparkDriver \
  --master local[4] \
  /target/ua.edu.sumdu.dl.plachert.jar \
  --debug --documents 1,4 --no-save
```

will run **Plachert** in debug mode for documents with ids {1,4} and will not save newly created shinglesAmount into database.


## License

MIT