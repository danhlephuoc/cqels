package org.deri.cqels.engine.windows;

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;

public class DSIRBTreeIndex extends DSIMap{
	
	public DSIRBTreeIndex(){
		index =new Long2ObjectRBTreeMap<Entry>();
	}
	

}
