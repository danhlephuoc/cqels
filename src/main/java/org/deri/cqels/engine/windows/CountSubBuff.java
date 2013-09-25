package org.deri.cqels.engine.windows;


public class CountSubBuff extends SubWindowBuff {
	long count,length;
	DiskBuff disk;

	public CountSubBuff(DiskBuff disk,int count) {
		super(disk.domains());
		this.disk=disk;
		this.count=count;
		length=0;
	}
	
	@Override
	public void add(long time, long[] vals) {
		super.add(time, vals);
		length++;
		if(!ignorePurge) purge();
		System.out.println(length+ "--"+size());
	}
	
	public int size() {
		return (int)length;
	}
	
	
	@Override
	public StreamItem remove() {
		System.out.println("CountSubBuff.remove");
		StreamItem tmp=super.remove();
		if(tmp!=null)
			length--;
		System.out.println("CountSubBuff.remove "+(tmp==null));
		return tmp;
	}
	
	@Override
	public void purge(){
		if(ignorePurge) return;
		while(length>0&&disk.size()>count) remove();
	}
}
