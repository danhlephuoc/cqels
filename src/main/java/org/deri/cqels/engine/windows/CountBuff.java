package org.deri.cqels.engine.windows;



public class CountBuff extends RingWindowBuff {
	long count,length;
	public CountBuff(WindowIndex[] domains,int count) {
		super(domains);
		this.count=count;
		length=0;
	}
	@Override
	public void add(long time, long[] vals) {
		super.add(time, vals);
		length++;
		purge();
	}
	
	public synchronized void purge(){
		while(length>count) remove();
	}
	
	@Override
	public StreamItem remove() {
		StreamItem tmp=super.remove();
		if(tmp!=null)
			length--;
		return tmp;
	}
	
	@Override
	public int size() {
		return (int)length;
	}
}
