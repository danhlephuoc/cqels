package org.deri.cqels.engine.winbuff;

import org.deri.cqels.engine.opimpl.Accumulator;
import org.deri.cqels.engine.opimpl.POOL;

public class DomAggEntry extends DomEntry {

	protected Accumulator acc;
	public DomAggEntry() {}
	public void setAcc(Accumulator acc){
		this.acc=acc;
	}
	public void update(MU mu){
		incCount();
	}
	
	public void expire(MU mu) 
	{ decrCount(); }

	public Object accVal() {
		// TODO Auto-generated method stub
		return count();
	}
	
	public PoolableObject newObject() {
		return (PoolableObject)POOL.DomAggEntry.borrowObject();
	}
	
	public void releaseInstance() {
		POOL.DomAggEntry.returnObject(this);
	}
	public void reset(MU mu){
		reset();
	}
}
