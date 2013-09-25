package org.deri.cqels.engine.winbuff;

import java.util.Iterator;

public class RingIterator implements Iterator<MU> {
	MUN start,cur;
	long startTick,endTick;
	int linkId,count,visited;
	public RingIterator(MUN start,long startTick,long endTick,int linkId,int count) {
		this.start=start;
		this.startTick=startTick;
		this.endTick=endTick;
		this.linkId=linkId;
		this.count=count;
		rewind();
	}
	
	public void rewind(){
		cur=start;
		visited=0;
		move();
	}
	
	public boolean hasNext() {
		if(visited>=count||cur==null||Math.abs(cur.timestamp)<Math.abs(endTick)) return false;
		return true;
	}
	
	public MUN getLink(MU item){
		if(item.getLink() instanceof MultipleLinks)
			return(MUN)((MultipleLinks)cur.getLink()).getLink(linkId);
		else return (MUN)cur.getLink();
	}
	
	public void move(){
		if(startTick<cur.timestamp){
			cur=getLink(cur);			
			if(cur!=null) move();
			visited++; 
		}
	}
	
	public MU next() {
		MUN tmp=cur;
		cur=getLink(cur);
		visited++;
//		if(visited>500) {
//			System.out.println(" too much "+visited +"for "+count+" "+cur.timestamp +" "+endTick);
//		}
		return tmp;
	}

	public void remove() {
		// TODO Auto-generated method stub

	}

}
