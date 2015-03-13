# CQELS language #

CQELS language is a declarative query language built from  SPARQL 1.1 [grammar](http://www.w3.org/TR/sparql11-query/#grammar) under the EBNF notation.

We add a query pattern to present window operators on RDF Stream into the `GraphPatternNotTriples` pattern.

```
GraphPatternNotTriples ::= GroupOrUnionGraphPattern| OptionalGraphPattern
|MinusGraphPattern|GraphGraphPattern| *StreamGraphPattern*|ServiceGraphPattern|Filter|Bind
```
Assuming that each stream has an IRI as identification, the `StreamGraphPattern` pattern is defined as follows.

```
StreamGraphPattern ::= ‘STREAM’ ‘[’ Window ‘]’ VarOrIRIref ‘{’TriplesTemplate‘}’
Window ::= Rangle|Triple|‘NOW’|‘ALL’
Range ::= ‘RANGE’ Duration (‘SLIDE’ Duration |'TUMBLING')?
Triple ::= ‘TRIPLES’ INTEGER
Duration ::= (INTEGER ‘d’|‘h’|‘m’|‘s’|‘ms’|‘ns’)+
```

where `VarOrIRIRef` and `TripleTemplate` are patterns for the variable/IRI and triple template of [SPARQL 1.1](http://www.w3.org/TR/sparql11-query/), respectively. Range corresponds to a time-based window while Triple corresponds to a triple-based window. The keyword SLIDE is used for specifying the sliding parameter of time-based window, whose time interval is specified by Duration.

`[NOW]` window is used to indicate that only the triples at the current timestamp are kept.


`[ALL]` window is used to indicate that all the triples will be kept in the window.

Query 1
```
PREFIX lv: <http://deri.org/floorplan/>
PREFIX dc: <http://purl.org/dc/elements/1.1/> 
PREFIX foaf: <http://xmlns.com/foaf/0.1/> 
SELECT ?locName  
FROM NAMED <http://deri.org/floorplan/>
WHERE {
STREAM <http://deri.org/streams/rfid> [NOW] 
{?person lv:detectedAt ?loc} 
{?person foaf:name "AUTHORNAME"^^<http://www.w3.org/2001/XMLSchema#string> }
GRAPH <http://deri.org/floorplan/> 
{?loc lv:name ?locName}
}
```

Query 2
```
PREFIX lv: <http://deri.org/floorplan/>
SELECT  ?person1 ?person2 
FROM NAMED <http://deri.org/floorplan/>
WHERE {
GRAPH <http://deri.org/floorplan/> 
{?loc1 lv:connected ?loc2}
STREAM <http://deri.org/streams/rfid> [NOW] 
{?person1 lv:detectedAt ?loc1} 
STREAM <http://deri.org/streams/rfid> [RANGE 3s] {?person2 lv:detectedAt ?loc2}
}
```

Query 3
```
PREFIX lv: <http://deri.org/floorplan/>
PREFIX dc: <http://purl.org/dc/elements/1.1/> 
PREFIX foaf: <http://xmlns.com/foaf/0.1/> 
SELECT  ?locName 
FROM NAMED <http://deri.org/floorplan/>
WHERE {
GRAPH <http://deri.org/floorplan/> 
{?loc lv:name ?locName}
STREAM <http://deri.org/streams/rfid> [TRIPLES 1] 
{?auth lv:detectedAt ?loc} 
STREAM <http://deri.org/streams/rfid> [RANGE 5s] {?coAuth lv:detectedAt ?loc}
{?paper dc:creator ?auth. ?paper dc:creator ?coAuth.
 ?auth foaf:name "AUTHORNAME"^^<http://www.w3.org/2001/XMLSchema#string> }
FILTER(?auth!=?coAuth)
}
```

Query 4
```
PREFIX lv: <http://deri.org/floorplan/>
PREFIX dc: <http://purl.org/dc/elements/1.1/> 
PREFIX foaf: <http://xmlns.com/foaf/0.1/> 
PREFIX dcterms: <http://purl.org/dc/terms/> 
PREFIX swrc: <http://swrc.ontoware.org/ontology#>
SELECT ?editorName WHERE{
STREAM <http://deri.org/streams/rfid> [TRIPLES 1] {?auth lv:detectedAt ?loc1} 
STREAM <http://deri.org/streams/rfid> [RANGE 15s] {?editor lv:detectedAt ?loc2}
GRAPH <http://deri.org/floorplan/> {?loc1 lv:connected ?loc2} 
?paper dc:creator ?auth. ?paper dcterms:partOf ?proceeding.
?proceeding swrc:editor ?editor. ?editor foaf:name ?editorName.
 ?auth foaf:name "AUTHORNAME"^^<http://www.w3.org/2001/XMLSchema#string>
}
```

Query 5
```
PREFIX lv: <http://deri.org/floorplan/>
PREFIX dc: <http://purl.org/dc/elements/1.1/> 
PREFIX foaf: <http://xmlns.com/foaf/0.1/> 
SELECT ?loc2 ?locName (count(distinct ?coAuth) as ?noCoAuths) 
FROM NAMED <http://deri.org/floorplan/>
WHERE {
GRAPH <http://deri.org/floorplan/> 
{?loc2 lv:name ?locName.?loc2 lv:connected ?loc1}
STREAM <http://deri.org/streams/rfid> [TRIPLES 1] 
{?auth lv:detectedAt ?loc1} 
STREAM <http://deri.org/streams/rfid> [RANGE 30s] {?coAuth lv:detectedAt ?loc2}
{?paper dc:creator ?auth. ?paper dc:creator ?coAuth.
 ?auth foaf:name "AUTHORNAME"^^<http://www.w3.org/2001/XMLSchema#string> }
FILTER(?auth!=?coAuth)
}
GROUP BY ?loc2 ?locName
```

Multiple Stream joins (for ISWC 2011 paper)
CONSTRUCT  {?s12 ?p2 o2. ?s13 ?p3 ?o3.} 
WHERE {
STREAM <http://deri.org/streams/L1> [RANGE 3s SLIDE 1s] {?s12 ?p1 ?o1}
STREAM <http://deri.org/streams/L2> [RANGE 3s TUMBLING] {?s12 ?p2 ?o2}
STREAM <http://deri.org/streams/L1> [RANGE 3s TUMLING] {?s13 ?p1 ?o1}
STREAM <http://deri.org/streams/L3> [TRIPLE 5] {?s13 ?p3 ?o3}
}}}}
  ```