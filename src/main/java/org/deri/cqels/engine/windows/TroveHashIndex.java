package org.deri.cqels.engine.windows;

import java.util.Iterator;

import gnu.trove.map.hash.TLongObjectHashMap;


public class TroveHashIndex extends WindowIndex {
	TLongObjectHashMap<Entry> index;
	
	public TroveHashIndex(){
		index =new TLongObjectHashMap<Entry>();
	}
	public Entry get(long idx) {
		return index.get(idx);
	}

	public void put(long idx, Entry entry) {
		index.put(idx, entry);
	}
	@Override
	public int size() {
		return index.size();
	}
	@Override
	protected void _remove(long idx) {
		index.remove(idx);
		
	}
	@Override
	public Iterator<Long> keys() {
		// TODO implement key iterator
		return null;
	}

}
