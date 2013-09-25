package org.deri.cqels.engine.windows;

public abstract class StreamItem {
	 public static final byte ONE=1,TWO=2;
	 public long[] vals;
		 
	 //Initialize two objects as lockers of the object pools
	 
	 public abstract StreamItem getPrv(byte idx);
	 public long time;
	 public StreamItem(){
		 time=System.nanoTime();
	 }
	 public abstract void load(WindowIndex[] domains,long[] vals);
	  
	 public abstract void invalidate(WindowIndex[] indexes);
}
