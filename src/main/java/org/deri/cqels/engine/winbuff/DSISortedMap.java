package org.deri.cqels.engine.winbuff;



import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;

public class  DSISortedMap extends DSIMap {

	public DSISortedMap(PoolableObjectFactory entryFactory) {
		super(1,entryFactory);
	}
	
	public DSISortedMap(int type,PoolableObjectFactory entryFactory){
		super(type,entryFactory);
	}
	
	public long getFirst(){ return ((Long2ObjectSortedMap<DomEntry>)index).firstKey();}
	public long getLast(){ return ((Long2ObjectSortedMap<DomEntry>)index).lastKey();}

}
