package org.deri.cqels.engine.windows;

import java.util.Iterator;

import cern.colt.map.OpenLongObjectHashMap;


public class ColtHashIndex extends WindowIndex {
	OpenLongObjectHashMap index;
	
	public ColtHashIndex(){
		index =new OpenLongObjectHashMap();
	}
	public Entry get(long idx) {
		return (Entry)index.get(idx);
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
		index.removeKey(idx);
		
	}
	@Override
	public Iterator<Long> keys() {
		// TODO Auto-generated method stub
		return (Iterator<Long>)index.keys().toList().iterator();
	}

}
