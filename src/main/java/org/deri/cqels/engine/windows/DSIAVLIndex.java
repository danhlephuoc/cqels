package org.deri.cqels.engine.windows;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;

public class DSIAVLIndex extends DSIMap {

	public DSIAVLIndex(){
		index =new Long2ObjectAVLTreeMap<Entry>();
	}
}
