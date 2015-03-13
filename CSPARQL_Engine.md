### Installation ###

The C-SPARQL engine can be downloaded at [http://streamreasoning.org/download](http://streamreasoning.org/download)


### Running Experimental Evaluation ###

The source code the [experiment](http://code.google.com/p/cqels/wiki/Experiments) with C-SPARQL can be downloaded at http://cqels.googlecode.com/files/csparql.jar. To prepare experiment data, download from http://cqels.googlecode.com/files/csparql_data.zip,then, extract the data into csparql data directory for example, `/home/user/csparql/data`

```
export MINMEM=2048
export MAXMEM=4096
export RUNMODE=single
export CSPARQLDATA=/home/user/csparql/data
export CSPARQLHOME=/home/user/csparql
export AUTHORNO=10
export STREAMSIZE=10000
export SLEEP=30
export DATASIZE=100k
epxort i=3
export AUTHORNO=5

java -Xms${MINMEM}m -Xmx${MAXMEM}m -jar csparql.jar $RUNMODE $CSPARQLDATA $CSPARQLHOME query$i $DATASIZE $STREAMSIZE $AUTHORNO
```