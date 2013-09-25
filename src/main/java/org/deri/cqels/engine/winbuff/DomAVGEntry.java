package org.deri.cqels.engine.winbuff;

public class DomAVGEntry extends DomSumEntry{
	public DomAVGEntry() {}
	
	
	public Object accVal() {
		return (Long)super.accVal()/count();
	}

}
