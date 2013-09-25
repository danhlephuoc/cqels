package org.deri.cqels.engine.winbuff;

public interface MultipleLinks extends LinkedItem {
	public void setLink(int id, LinkedItem item);
	public LinkedItem getLink(int id);
}
