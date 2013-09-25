package org.deri.cqels.engine.winbuff;

public class NLinks implements MultipleLinks {
	LinkedItem[] links;
	
	public NLinks(int length){ links= new LinkedItem[length];}
	public LinkedItem getLink() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLink(LinkedItem item) {
		
	}

	public void setLink(int id, LinkedItem item) {
		links[id]=item;
	}

	public LinkedItem getLink(int id) {
		return links[id];
	}

}
