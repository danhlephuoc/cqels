package org.deri.cqels.engine.winbuff;

import org.deri.cqels.engine.opimpl.PhysicalOp;
import org.deri.cqels.engine.opimpl.ProcQueue;

public abstract class AWBBase implements AWB, RoutingMessage {
	protected PhysicalOp from;
	public void setFrom(PhysicalOp from) { this.from=from;}
	public PhysicalOp from() {
		// TODO Auto-generated method stub
		return from;
	}
}
