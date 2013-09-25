package org.deri.cqels.engine.windows;

import java.util.Iterator;

import com.hp.hpl.jena.util.iterator.NullIterator;

public abstract class RingWindowBuff implements WindowBuff{
	public LinkedStreamItemPool items=new LinkedStreamItemPool();
	public WindowIndex[] domains;
	public RingWindowBuff(WindowIndex[] domains){
		this.domains= domains;
	}
	
	public RingWindowBuff(byte size){
		domains=new DSIOpenHashIndex[size];
		for(byte i=0;i<size;i++)
			domains[i]=new DSIOpenHashIndex();
	}
	
	
	
	public synchronized void add(long time,long[]vals){
		StreamItem itm;
		itm=StreamItemPool.alloc((byte)domains.length);
		items.add(itm);
		itm.load(domains, vals);
		itm.time=time;
		//TODO : not all the buff need to be purge after adding new items
		//purge();
	}
	
	public WindowIndex[] domains() {
		return domains;
	}
	
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void add(long[]vals){
		add(System.nanoTime(),vals);
	}
	
	public Iterator<long[]> search(byte i,long val){
	    purge();
		Entry entry= (Entry)domains[i].get(val);
		if(entry!=null&&entry.count>0)		
			return new StreamValIter(entry.eor,i,entry.count);
		return NullIterator.instance();
	}
	
	public StreamItem remove(){
		System.out.println("remove -Ring Window Buff");
		StreamItem tmp=items.poll();
		if(tmp!=null){
			tmp.invalidate(domains);
			StreamItemPool.dealloc(tmp);
		}
		System.out.println("remove -Ring Window Buff" +(tmp==null));
		return tmp;
	}
	
	public Iterator<StreamItem> iterator(){
		return new StreamItemItr(items.first());
	}
	
	public abstract void purge();
	
	public void release(){
		//TODO:
		StreamItemPool.deallocBuff(this);
		
		//TODO
		for(WindowIndex domain:domains)
			domain.release();
	}
	
	class StreamValIter implements Iterator<long[]>{
		byte idx;
		StreamItem itm;
		int count,visited;
		public StreamValIter(StreamItem itm,byte idx,int count){
			this.itm=itm;
			this.idx=idx;
			this.count=count;
			visited=0;
		}

		public boolean hasNext() {
			return (itm!=null)&&(visited<count);
		}

		public long[] next() {
			StreamItem tmp=itm;
			if(itm!=null) itm=itm.getPrv(idx);
			visited++;
			//System.out.println("next " +tmp.vals[0]);
			return tmp.vals;
		}

		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}

}
