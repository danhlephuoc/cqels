package org.deri.cqels.engine.winbuff;

import java.util.Iterator;

public interface AWB {
	public void insert(MU m);
	public Iterator<MU> probe(MU m,long tick);
	public void purge(long tick);
}
