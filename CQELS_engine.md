## CQELS engine ##

### Getting started ###

Please check [Getting Started](http://code.google.com/p/cqels/wiki/cqels_getting_started) for how to build application on top of CQELS engine.

### Installation ###

The CQELS engine can be downloaded as  [jar file](https://dl.dropboxusercontent.com/u/23882305/CQELS/cqelsv1.0.1.jar).



### Running the experiment codes ###
The experiment data can be downloaded at http://cqels.googlecode.com/files/cqels_data.zip. Due to the limit of uploading file,  the DBLP dataset at 10 million triples can be downloaded in separate file http://cqels.googlecode.com/files/10M.rdf.bz2. Extract the experiment data, then put in the data directory of CQELS engine, for example ,`/home/user/cqels/data`
```
export MINMEM=2048
export MAXMEM=4096
export RUNMODE=single
export CQELSDATA=/home/user/cqels/data
export CQELSHOME=/home/user/cqels
export STREAMSIZE=10000
export SLEEP=30
export DATASIZE=100k
export PRELOADED=true
export AUTHORNO=1

java -Xms${MINMEM}m -Xmx${MAXMEM}m -jar cqelsv1.0.0.jar $RUNMODE $CQELSDATA $CQELSHOME query1 $DATASIZE $STREAMSIZE $PRELOADED $AUTHORNO
```

### Using the CQELS engine ###