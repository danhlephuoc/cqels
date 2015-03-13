# Getting Started #

Following _`TextStream`_ class illustrates how to implement a class for streamming RDF data by extends RDFStream class, the [RDFStream class](http://cqels.googlecode.com/files/RDFStream.java) provides the "stream" method to stream RDF triples to the CQELS engine

```

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.engine.RDFStream;
import com.hp.hpl.jena.graph.Node;

public class TextStream extends RDFStream implements Runnable{
	String txtFile;
	boolean stop=false;
	long sleep=500;
	public TextStream(ExecContext context, String uri,String txtFile) {
		super(context, uri);
		this.txtFile=txtFile;
	}

	@Override
	public void stop() {
		stop=true;
	}
	public void setRate(int rate){
		sleep=1000/rate;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			BufferedReader reader = new BufferedReader(new FileReader(txtFile));
			String strLine;
			while ((strLine = reader.readLine()) != null &&(!stop))   {
			    String[] data=strLine.split(" ");
				stream(n(data[0]),n(data[1]),n(data[2])); // For streaming RDF triples
				
				if(sleep>0){
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static  Node n(String st){
		return Node.createURI(st);
	}

}

```

Initialize the CQELS engine

```

ExecContext context=new ExecContext(HOME, false);
```

The static datasets will be loaded directly from the URIs of the named graphs, they can also can be load by following codes :

```

//load the default dataset from directory

context.loadDefaultDataset("{DIRECTORY TO LOAD DEFAULT DATASET}");

//load datasets for named graph

 context.loadDataset("{URI OF NAMED GRAPH}", "{DIRECTORY TO LOAD NAMED GRAPH}");
```

Initialize the RDF stream

```
RDFStream stream = new TextStream(context, "http://deri.org/streams/rfid", HOME+"/rfid_50000.stream");
```

Register the query to CQELS engine
```

String queryString =" ... ";//put your query here

//With the select-type query
ContinuousSelect selQuery=context.registerSelect(queryString);
selQuery.register(new ContinuousListener()
{
      public void update(Mapping mapping){
         String result="";
	 for(Iterator<Var> vars=mapping.vars();vars.hasNext();)
         //Use context.engine().decode(...) to decode the encoded value to RDF Node
		result+=" "+ context.engine().decode(mapping.get(vars.next()));
         System.out.println(result);
      } 
});
//With the construct-type query
ContinuousConstruct consQuery=context.registerConstruct(queryString);
consQuery.register(new ConstructListener(context) {
			
	@Override
	public void update(List<Triple> graph) {
		for(Triple t : graph) {
	              System.out.println(t.getSubject() + " " + t.getPredicate() + " " + t.getObject());
		}
	}};)
```

Start streaming thread
```
(new Thread(stream)).start();

```