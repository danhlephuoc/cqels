package org.deri.cqels.engine.windows;

import java.util.Iterator;


public abstract class WindowIndex {
	static final int purgeRatio=25;
	ExpiredKey first,last;
	protected int expCount=0;
	public abstract Entry get(long idx);
	public abstract void put(long idx,Entry entry);
	public abstract int size();
	public abstract Iterator<Long> keys();
	protected abstract void _remove(long idx);
	
	public Entry putFirstEntry(long val,Entry entry){
		if(entry==null){
			entry=new Entry();
			put(val,entry);
		}
		else expCount--;
		entry.count=1;
		return entry;
	}
	
	public boolean purge(){
		boolean purged=false;
		if(first!=null){
			Entry entry=get(first.key);
			if(entry!=null&&entry.count==0){
				_remove(first.key);
				purged=true;	
				expCount--;
			}
			ExpiredKey tmp=first;
			first=first.next;
			ExpiredKey.dealloc(tmp);
		}
		return purged;	
	}
	public void remove(long idx){
		ExpiredKey tmp=ExpiredKey.alloc(idx);
		if(first==null||last==null){
			first=tmp;
			last=first;
		}
		else{
			last.next=tmp;
			last=tmp;
			last.next=null;
		}
		expCount++; //int count=0;
		while(expCount>0&&100*(long)expCount>((long)purgeRatio)*((long)size())){
		
			purge();
			//count++;
			//if(count>4) 
				//System.out.println("stuck here "+expCount +" "+size() +" "+ (first!=null));
		}
	}
	
	public void release(){
		//TODO
	}
}
