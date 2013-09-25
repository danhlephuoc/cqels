package org.deri.cqels.engine.winbuff;

import org.deri.cqels.engine.opimpl.PhysicalOp;

public interface RoutingMessage {

	public PhysicalOp from();
	public void setFrom(PhysicalOp from);
}
