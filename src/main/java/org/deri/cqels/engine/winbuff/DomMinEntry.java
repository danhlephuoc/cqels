package org.deri.cqels.engine.winbuff;

import it.unimi.dsi.fastutil.longs.Long2IntAVLTreeMap;

import org.deri.cqels.engine.opimpl.POOL;

public class DomMinEntry extends DomAggEntry{
	long min;
	DSISortedMap map;
	public DomMinEntry() {
		map=new DSISortedMap(POOL.DomEntry);
	}
	
	public void update(MU mu){
		long idx=mu.get(acc.accVar());
		DomEntry entry=map.get(idx);
		if(entry==null)
		{
			if(idx<min) min=idx;
			entry=(DomEntry)POOL.DomEntry.borrowObject();
			entry.reset();
			map.put(idx, entry);
		}
		else entry.incCount();
	}
	
	public void expire(MU mu) 
	{ 
		long idx=mu.get(acc.accVar());
		DomEntry entry=map.get(idx);
		if(idx==min&&entry.count()==1){
			map.remove(idx);
			if(!map.isEmpty()) min=map.getFirst();
			else min=Long.MAX_VALUE;
			return;	
		}
		entry.decrCount();
	}
	
	@Override
	public void reset(MU mu) {
		if(!map.isEmpty()) map.release();
		min=Long.MAX_VALUE;
		update(mu);
		reset();
	}
	
	public Object accVal() {
		return min;
	}

}
