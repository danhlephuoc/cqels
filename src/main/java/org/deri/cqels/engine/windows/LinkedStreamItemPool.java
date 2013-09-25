package org.deri.cqels.engine.windows;

public class LinkedStreamItemPool extends StreamItemPool {
	public LinkedStreamItem first,last;
	
	public synchronized void add(StreamItem itm){
		if(first==null && last==null){
			first=(LinkedStreamItem)itm;
			last=(LinkedStreamItem)itm;
		}
		last.next=(LinkedStreamItem)itm;
		last=(LinkedStreamItem)itm;
	}
	
	public synchronized void add(StreamItemPool other){
		//System.out.println("added");
		if(first==null && last==null){
			first=(LinkedStreamItem)other.first();
			last=(LinkedStreamItem)other.last();
			return ;
		}
		last.next=(LinkedStreamItem)other.first();
		last=(LinkedStreamItem)other.last();
	}
	
	public synchronized StreamItem first(){ return first;};
	
	public synchronized StreamItem last(){ return last;};
	
	public synchronized StreamItem poll(){
		//System.out.println("poll");
		if(first!=null){
				LinkedStreamItem tmp=first;
				first=tmp.next;
				tmp.next=null;
				return tmp;
		}
		//System.out.println("null "+(first==null));
		return null;	
	}
}
