package org.deri.cqels.engine.winbuff;

import java.util.Iterator;
import org.deri.cqels.engine.opimpl.PhysicalOp;

public class BatchBuff extends AWBBase {
	Entry head,last;
	boolean ready=false;
	Visistor visistor;
	
	public BatchBuff(){
	}
	public void set(PhysicalOp from){
		super.setFrom(from);
		head=null;
		last=null;
	}
	public void insert(MU m) {
		Entry entry=(Entry)Entry.defaultFactory.borrowObject();
		entry.set(m, head);
		if(last==null) last=entry;
		head=entry;
	}

	public Iterator<MU> probe(MU m, long tick) {
		// TODO this method won't be needed in for this buffer
		return null;
	}
	
	public Iterator<MU> iterator(){
		return new MUIter(head);
	}
	public Entry getFirst(){ return head;}
	public Entry getLast(){ return last;}
		
	public void ready(){
		ready=true;
		/*if(head!=null)
			for(Iterator<PhysicalOp> itr=ops.iterator();itr.hasNext();)
				itr.next().newMapping(head.elm, this);*/
	}

	public  boolean isReady() { return ready;}
	public static class MUIter implements Iterator<MU>{
		Entry cur;
		public MUIter(Entry head){
			cur=head;
		}
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return cur!=null;
		}

		public MU next() {
			Entry tmp=cur;
			cur=cur.next;
			// TODO Auto-generated method stub
			return tmp.elm;
		}

		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}
	public static class Entry implements PoolableObject{
		public static PoolableObjectFactory defaultFactory=new PoolableObjectFactory() {
			
			@Override
			public PoolableObject newObject() {
				// TODO Auto-generated method stub
				return new Entry();
			}
		};
		public MU elm=null;
		public Entry next=null;
		Entry(){};
		public void set(MU elm,Entry next){
			this.elm=elm;
			this.next=next;
		}
		public PoolableObject newObject() {
			// TODO Auto-generated method stub
			return (PoolableObject)defaultFactory.borrowObject();
		}
		public void releaseInstance() {
			defaultFactory.returnObject(this);
		}
		
	}
	
	public void purge(long tick) {
		// TODO Don't support purging by tick	
	}
}
