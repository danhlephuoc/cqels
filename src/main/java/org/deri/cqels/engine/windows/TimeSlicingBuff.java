package org.deri.cqels.engine.windows;

public class TimeSlicingBuff extends RingWindowBuff {
	long duration;
	public TimeSlicingBuff(WindowIndex[] domains,long duration) {
		super(domains);

		this.duration=duration;
	}
	
	@Override
	public void purge(){
		if(items.last()!=null&&(items.last().time+duration<System.nanoTime())){
			StreamItemPool.deallocBuff(this);
			synchronized (items) {
				items.first=null;
				items.last=null;
			}			
		}
		while(items.first()!=null&&(items.first().time+duration<System.nanoTime()))
			remove();
	}

}
