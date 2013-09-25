package org.deri.cqels.engine.winbuff;

import org.deri.cqels.engine.opimpl.POOL;

public class LinkedMUJ extends MUJ {
	
	public LinkedItem link;
	

	public PoolableObject newObject() {
		return (PoolableObject)POOL.LinkedMUJ.borrowObject();
	}

	@Override
	protected void release() {
		POOL.LinkedMUJ.returnObject(this);
	}

	
	public LinkedItem getLink() {
		return link;
	}

	public void setLink(LinkedItem item) {
			this.link=item;
	}

}
