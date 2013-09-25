package org.deri.cqels.engine.winbuff;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import org.deri.cqels.engine.opimpl.POOL;
import org.deri.cqels.engine.opimpl.PhysicalOpBase;
import org.deri.cqels.engine.opimpl.SingleThreadProcQueue;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.util.iterator.NullIterator;

public class CountWindow extends PhysicalOpBase implements AWB {
	ArrayDeque<MUN> buff,expBuff;
	DomainIndex[] indexes;
	int winCount;
	CircularRange ranges=new CircularRange();
	Var[] vars;
	HashMap<Var, Integer> map;
	PoolableObjectFactory linkFactory;
	public CountWindow(ExecutorService exec,DomainIndex[] indexes,Var[] vars,int winCount){
		super();
		this.indexes = indexes;
		map = new HashMap<Var, Integer>();
		this.vars = vars;
		this.winCount = winCount;
		buff = new ArrayDeque<MUN>();
		expBuff = new ArrayDeque<MUN>();
		queue=new SingleThreadProcQueue();
		final int length = indexes.length;
		if(length>2)
		{
			linkFactory=new PoolableObjectFactory() {
			
			@Override
			public Object newObject() {
				// TODO Auto-generated method stub
				return new NLinks(length);
			}
			}; 
		}
		else if(length==2) linkFactory=POOL.TwoLinks;
		for(int i=0;i<vars.length;i++) map.put(vars[i], i);
	}
	
	public void insert(MU m) 
	{
		if(buff.size()>=winCount)
		{
		  ranges.add(buff.peekFirst().timestamp, buff.peekLast().timestamp);	
		  MUN expired=buff.poll();
		  expired.timestamp=-expired.timestamp;
		  expBuff.add(expired);
		  expired.setFrom(this);
		  queue.queue(expired);
		  queue.deque();
		}
		m.setFrom(this);
		buff.add((MUN)m);
		
		//prepare the skeleton linked item for the domain index
		if(indexes.length==1) m.setLink(m);
		else {
			//create two links or more for two indexs
			LinkedItem links=(LinkedItem)linkFactory.borrowObject();
			m.setLink(links);
		}
	
			
		//Update indexes
		for(int i=0;i<indexes.length;i++)
			indexes[i].add(i,m.get(vars[i]), m);
		
		//queue the new item to the output queue
		queue.queue(m);
		//signal for comsuming the output queue
		queue.deque();
	}
	
	public void newMapping(MU obj) {
		// TODO Auto-generated method stub
		
	}
	public void expireMapping(MU mu) {
		// TODO Auto-generated method stub
		
	}	
	
	
	public Iterator<MU> probe(MU m, long tick) {
		return probe(getProbeIdx(m),m,tick);
	}
	
	public Iterator<MU> probe(int id,MU m, long tick) {
		if(id==-1) return NullIterator.instance();
		DomEntry entry=indexes[id].get(m.get(vars[id]));
		if(entry!=null)
			return new RingIterator((MUN)entry.getLink(), tick, getEndTick(tick),
				id, entry.count()); 
		return NullIterator.instance();
	}
	
	public Iterator<MU> probe(int id,long key, long tick) {
		if(id==-1) return NullIterator.instance();
		DomEntry entry=indexes[id].get(key);
		if(entry!=null)
		return new RingIterator((MUN)entry.getLink(), tick, getEndTick(tick),
				id, entry.count());
		return NullIterator.instance();
	}
	public long getEndTick(long tick){
		int idx=ranges.get(tick);
		//if(idx>=0) return ranges.ends[idx];
		if(idx>=0) return ranges.starts[idx];
		return buff.peekLast().timestamp;
	}
	
	public int getProbeIdx(MU m){
		for(int i=0;i<vars.length;i++){
			if(m.get(vars[i])!=-1) return i;
		}
		return -1;
	}
	
	public void purge(long tick) {
		synchronized (indexes) {
			while(expBuff.size()>0&&Math.abs(expBuff.peek().timestamp)<=tick){
				MUN release=expBuff.poll();
				for(int i=0;i<vars.length;i++){
					indexes[i].remove(map.get(vars[i]),release.get(vars[i]), release);
				}
				release.releaseInstance();
				ranges.advance(tick);
			}
		}

	}
	
	class CircularRange{
		final static int MAX=1000;
		public long[] starts=new long[MAX],ends=new long[MAX];
		int first=-1, last=-1;
		public CircularRange(){	}
		public void add(long start, long end){
			if(first==-1){
				first=0;
				last=0;
			}
			last=incr(last);
			starts[last]=start;
			ends[last]=end;
		}
	
		
		public void advance(long tick){
			if(first!=-1){
				int tmp=first;
				while(tick<starts[tmp]){
					if(tmp==last){
						first=-1;
						last=-1;
						return;
					}
					tmp=incr(tmp);
				}
				first=tmp;
			}
		}
		
		public int get(long tick){
			int tmp=first;
			while(tmp!=-1){
				if(tick<=ends[tmp])
					return tmp;
				if(tmp==last) tmp=-1;
				else
					tmp=incr(tmp);
			}
			return -1;
		}
		public int incr(int idx){ int n=idx+1; if(n==MAX) n=0; return n;}
	}

	

}
