package org.deri.cqels.engine.windows;

import java.util.Iterator;

import com.carrotsearch.hppc.LongObjectOpenHashMap;

public class HppcHIndex extends WindowIndex {
	LongObjectOpenHashMap<Entry> index;
	
	public HppcHIndex(){
		index =new LongObjectOpenHashMap<Entry>();
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
		// TODO implement keys iterator
		return null;
	}

}
