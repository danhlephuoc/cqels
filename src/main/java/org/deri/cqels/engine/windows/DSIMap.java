package org.deri.cqels.engine.windows;


import java.util.Iterator;

import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;

public class DSIMap extends WindowIndex {
	protected AbstractLong2ObjectMap<Entry> index;
	
	
	public Entry get(long idx) {
		return index.get(idx);
	}

	public synchronized void put(long idx, Entry entry) {
		index.put(idx, entry);
	}
	
	@Override
	protected synchronized void _remove(long idx) {
		index.remove(idx);		
	}
	
	@Override
	public int size() {
		return index.size();
	}
	 
	public Iterator<Long> keys(){
		return index.keySet().iterator();
	}
	
	@Override
	public void release() {
		index.clear();
	}
}
