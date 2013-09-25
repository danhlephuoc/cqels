package org.deri.cqels.engine.winbuff;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.deri.cqels.engine.opimpl.BatchDispatchOp;
import org.deri.cqels.engine.opimpl.POOL;

public class ToBeExpiredBuff extends BatchBuff {
	
	Visistor visistor;
	BatchDispatchOp batchOp;
	Object2ObjectOpenHashMap<MU, DomEntry> hash;
	public ToBeExpiredBuff(BatchDispatchOp batchOp){
		hash=(Object2ObjectOpenHashMap<MU, DomEntry>)POOL.DomEntryMap.borrowObject();
		visistor= new IncreaseCount(hash);
		this.batchOp=batchOp;
	}
	public void insert(MU m) {
		super.insert(m);
		visistor.visit(m);
	}

	public void purge(MU mu) {
		DomEntry entry=hash.get(mu);
		if(entry==null) return;
		BatchBuff.Entry cur=head,tmp,before=null;
		while(cur!=null&&entry.count()>0){
			tmp=cur.next;
			if(cur.elm.isExpired()){
				if(before!=null)
					before.next=tmp;
				else
					head=tmp;
				//TODO, is the mu contain same key as cur.elm
				entry.decrCount();
				MU expire=cur.elm;
				cur.releaseInstance();
				batchOp.expireOne(expire);
				cur=tmp;
			}
			else{
				before=cur;
				cur=cur.next;
			}
		}
	}
	
	public void addBuff(BatchBuff other){ 
		//TODO : update expiration count
		if(head!=null)
			last.next=other.getFirst();
		else{
			head=other.getFirst();
			last=other.getLast();
		}
	}
	
	
	
	public static class IncreaseCount implements Visistor{
		Object2ObjectOpenHashMap<MU, DomEntry> hash;
		public IncreaseCount(Object2ObjectOpenHashMap<MU, DomEntry> hash){
			this.hash=hash;
		}
		public void visit(MU mu) {
			if(mu instanceof MUN){
				DomEntry entry=hash.get(mu);
				if(entry!=null) entry.incCount();
				else {
					entry=(DomEntry)POOL.DomEntry.borrowObject();
					entry.setCount(1);
					hash.put(mu, entry);
				}
			}
		}
		
	}
}
