package org.deri.cqels.engine.windows;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class DSIOpenHashIndex extends DSIMap {
	
	public DSIOpenHashIndex()
	{
		index =new Long2ObjectOpenHashMap<Entry>();
	}

}
