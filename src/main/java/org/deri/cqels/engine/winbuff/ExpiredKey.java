package org.deri.cqels.engine.winbuff;

public class ExpiredKey{
	public long key;
	public ExpiredKey next;
	static ExpiredKey buff;
	public ExpiredKey(long key){
		this.key=key;
	}
	public static  ExpiredKey alloc(long key){
		if(buff==null)
			return new ExpiredKey(key);
		else{
			ExpiredKey tmp=buff;
			buff=buff.next;
			tmp.key=key;
			tmp.next=null;
			return tmp;
		}
	}
	public static void dealloc(ExpiredKey ref){
		ref.next=buff;
		buff=ref;
	}
}