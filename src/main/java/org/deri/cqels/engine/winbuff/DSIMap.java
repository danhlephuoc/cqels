package org.deri.cqels.engine.winbuff;


import java.util.ArrayDeque;
import java.util.Iterator;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMaps;

public class  DSIMap extends DomainIndex {
	protected AbstractLong2ObjectMap<DomEntry> index;
	
	public DSIMap(PoolableObjectFactory entryFactory) {
		index =new Long2ObjectOpenHashMap<DomEntry>();
		expiredKeys=new ArrayDeque<Long>();
		this.entryFactory=entryFactory;
	}
	
	public DSIMap(int type,PoolableObjectFactory entryFactory){
		this.entryFactory=entryFactory;
		expiredKeys=new ArrayDeque<Long>();
		if(type==1) index =new Long2ObjectAVLTreeMap<DomEntry>();
		else if(type==2)
			index =new Long2ObjectRBTreeMap<DomEntry>();
		else
			index =new Long2ObjectOpenHashMap<DomEntry>();
	}
	public DomEntry get(long idx) {
		return index.get(idx);
	}

	public synchronized void put(long idx, DomEntry entry) {
		index.put(idx, entry);
	}
	@Override
	protected synchronized void _remove(long idx) {
		DomEntry entry=index.remove(idx);
		entry.releaseInstance();
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return index.size();
	}
	 
	public Iterator<Long> keys(){
		return index.keySet().iterator();
	}
	
	@Override
	public void release() {
		index.clear();
	}
	public boolean isEmpty(){ return index.isEmpty();}
}
