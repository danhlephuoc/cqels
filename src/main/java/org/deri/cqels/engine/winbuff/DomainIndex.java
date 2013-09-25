package org.deri.cqels.engine.winbuff;

import java.util.ArrayDeque;
import java.util.Iterator;

public abstract class DomainIndex {
	static final int purgeRatio=25;
	ArrayDeque<Long> expiredKeys;
	public abstract DomEntry get(long idx);
	PoolableObjectFactory entryFactory;
	public abstract void put(long idx,DomEntry entry);
	
	public abstract int size();
	public abstract Iterator<Long> keys();
	protected abstract void _remove(long idx);
	
	public void add(int id,long idx, MU mu){
		//note: mu must be set a skeleton link
		DomEntry entry=get(idx);
		if(entry==null){
			entry=(DomEntry)entryFactory.borrowObject();
			entry.reset();
			put(idx,entry);
		}
		else entry.incCount();
		if(mu.getLink()==null) return;
		
		//Just one index, use the link of the mapping for linking the same key 
		if(mu.getLink() instanceof MU){
			LinkedItem tmp=entry.getLink();
			mu.setLink(tmp);
			entry.setLink(mu);
			return;
		}
		//for two indexes and more, use intermediate links
		if(mu.getLink() instanceof MultipleLinks){
			LinkedItem tmp=entry.getLink();			
			((MultipleLinks)mu.getLink()).setLink(id, tmp);
			entry.setLink(mu);
			return;
		}
	}
	
	public boolean purge(){
		boolean purged=false;
		if(!expiredKeys.isEmpty()){
			DomEntry entry=get(expiredKeys.peek());
			if(entry!=null&&entry.count()==0){
				_remove(expiredKeys.poll());
				purged=true;	
			}
			expiredKeys.poll();
		}
		
		return purged;	
	}
	
	public void remove(int id,long idx,MU mu){
		DomEntry entry=get(idx);
		entry.decrCount();
		//Don't need to update links from the domain value
		if(entry.count()<=0) remove(idx);
	}
	public void remove(long idx){
		expiredKeys.add(idx); 
		while(expiredKeys.size()*100/size()>purgeRatio){
			purge();
		}
	}
	
	public void release(){
		//TODO
	}
}
