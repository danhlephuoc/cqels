package org.deri.cqels.engine.windows;

import java.util.Iterator;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class StreamItr implements Iterator<long[]>{
	TupleInput input;
	boolean hasNext=false;
	long key,next;
	String name;
	public  StreamItr(Database db,long key){
		this.key=key;
		DatabaseEntry keyEnt=new DatabaseEntry();
		LongBinding.longToEntry(key, keyEnt);
		DatabaseEntry data=new DatabaseEntry();
		name=db.getDatabaseName();
		if(db.get(null, keyEnt, data, LockMode.DEFAULT)==OperationStatus.SUCCESS){
			hasNext=true;
			input=new TupleInput(data.getData());
			_read();
		}
	}
	
	private void _read(){
		try{				
			next=input.readLong();
			//System.out.println(name+"-->"+next);
		}
		catch(Exception e){
			hasNext=false;
		}
		if(next<0) hasNext=false;
	}
	
	public boolean hasNext() {
		return hasNext;
	}

	public long[] next() {	
		long[] arr={key,next};
		_read();
		return arr; 
	}

	public void remove() {
		// TODO Auto-generated method stub
	}
	
}
