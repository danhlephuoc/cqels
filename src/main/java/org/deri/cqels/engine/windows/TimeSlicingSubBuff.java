package org.deri.cqels.engine.windows;

public class TimeSlicingSubBuff extends SubWindowBuff {
	long duration;
	
	public TimeSlicingSubBuff(WindowIndex[] domains,long duration) {
		super(domains);

		this.duration=duration;
	}
	
	
	@Override
	public void purge(){
		//TODO: batch invalidate
		if(ignorePurge) return;
		while(items.first()!=null&&(items.first().time+duration<System.nanoTime()))
			remove();
	}

}
