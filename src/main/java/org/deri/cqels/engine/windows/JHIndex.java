package org.deri.cqels.engine.windows;

import java.util.Hashtable;
import java.util.Iterator;

public class JHIndex extends WindowIndex {
	Hashtable<Long, Entry> index;
	
	public JHIndex(){
		index =new Hashtable<Long, Entry>();
	}
	public Entry get(long idx) {
		return index.get(idx);
	}

	public void put(long idx, Entry entry) {
		index.put(idx, entry);
	}
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return index.size();
	}
	@Override
	protected void _remove(long idx) {
		index.remove(idx);		
	}
	@Override
	public Iterator<Long> keys() {
		// TODO Auto-generated method stub
		return index.keySet().iterator();
	}

}
