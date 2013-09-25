package org.deri.cqels.engine.windows;

import java.util.Iterator;

public interface WindowBuff {
	public WindowIndex[] domains();
	public void add(long[]vals);
	public Iterator<long[]> search(byte i,long val);
	public Iterator<StreamItem> iterator();
	public StreamItem remove();
	public void purge();
	public int size();
	
}
