package org.deri.cqels.engine.windows;

public abstract class SubWindowBuff extends RingWindowBuff {
	protected boolean ignorePurge=true;
	public SubWindowBuff(WindowIndex[] domains){
		super(domains);
	}
	
	public SubWindowBuff(byte size){
		super(size);
	}
	
	public synchronized void add(long time, long[] vals) {
		super.add(time, vals);
		if(!ignorePurge) purge();
	}
	public void setIgnorePurge(boolean igPurge){ ignorePurge=igPurge;}
}
