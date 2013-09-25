package org.deri.cqels.engine.windows;

import com.sleepycat.je.Environment;


public class TimeSlicingDiskBuff extends DiskBuff {
	long duration;
	public TimeSlicingDiskBuff(Environment env,byte cols,byte idxs,long duration) {
		super(env,cols,idxs);
		this.duration=duration;
		init();
		
	}
	
	public TimeSlicingDiskBuff(Environment env,byte cols,byte idxs,long duration,int segment) {
		super(env,cols,idxs,segment);
		this.duration=duration;
		init();
	}
	
	public void purge() {
		while(lastD.buff!=null&&lastD.buff.items.first().time+duration<System.nanoTime())
			remove();
	}

	@Override
	public SubWindowBuff initWin(WindowIndex[] domains) {
		// TODO Auto-generated method stub
		return new TimeSlicingSubBuff(domains,duration);
	}

}
