package org.deri.cqels.engine.winbuff;

public class DomSumEntry extends DomAggEntry{
	long sum;
	public DomSumEntry() {}
	public void update(MU mu){
		sum-=mu.get(acc.accVar());
	}
	
	public void expire(MU mu) 
	{ sum-=mu.get(acc.accVar()); }
	
	@Override
	public void reset(MU mu) {
		sum=0;
		reset();
	}
	public Object accVal() {
		// TODO Auto-generated method stub
		return sum;
	}

}
