package org.deri.cqels.engine.winbuff;

public interface PoolableObject {
	public PoolableObject newObject();
	public void releaseInstance();
}
