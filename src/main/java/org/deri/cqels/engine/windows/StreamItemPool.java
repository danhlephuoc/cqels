package org.deri.cqels.engine.windows;

public abstract class StreamItemPool {

	static StreamItemPool buff1=new LinkedStreamItemPool(),buff2=new LinkedStreamItemPool();
	
	public static void initLinkedPool(){
		buff1=new LinkedStreamItemPool();
		buff2=new LinkedStreamItemPool();
	}
	
	public static void initArrayDequePool(){
		buff1=new ArrayDequeStreamItemPool();
		buff2=new ArrayDequeStreamItemPool();
	}
	
	public abstract void add(StreamItem itm);
	
	public abstract void add(StreamItemPool other);
	public abstract  StreamItem first();
	public abstract  StreamItem last();
	public abstract StreamItem poll();
	
	public  static  void deallocBuff(RingWindowBuff buff){
		 if(buff.items.first() instanceof OneIdx) {
			 buff1.add(buff.items);
		 }
		 if(buff.items.first() instanceof TwoIdxes) {
			 buff2.add(buff.items);
		 }
		 
	 }
	public static StreamItem  alloc(byte i){
		 StreamItem tmp;
		 if(i==StreamItem.ONE){
			 tmp=buff1.poll();
			 if(tmp!=null) return tmp;
			 else return new OneIdx();
			
		 }
		 
		 tmp=buff2.poll();
		 		 
		 if(tmp!=null) return tmp;
		 else  return new TwoIdxes();
	 }
	public  static  void dealloc(StreamItem itm){
		 if(itm instanceof OneIdx){
			 buff1.add(itm);
			 return;
		 }
		 buff2.add(itm);
	 }

	/*public static   StreamItem poll(StreamItemPool buff){
		 if(buff.next!=null){
				StreamItem tmp=buff.next;
				buff.next=tmp.next;
				return tmp;
		 }
		 return null;
	 }*/

}
