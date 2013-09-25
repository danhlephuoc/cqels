package org.deri.cqels.engine.windows;

import java.util.Iterator;

public class StreamItemItr implements Iterator<StreamItem>{
	StreamItem cur=null;
	public StreamItemItr(StreamItem cur){
		this.cur=cur;
	}
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return cur!=null;
	}

	public StreamItem next() {
		StreamItem tmp=null;
		if(hasNext()){
			tmp=cur;
			//TODO: is it necessary?
			//cur=cur.next;
		}
		return tmp;
	}

	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
}
