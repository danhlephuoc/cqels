package org.deri.cqels.engine.winbuff;

public class TwoLinks implements MultipleLinks {
	LinkedItem first,second;
	
	public LinkedItem getLink() {
		// TODO Auto-generated method stub
		return first;
	}

	public void setLink(LinkedItem item) {
		first=item;
	}

	public void setLink(int id, LinkedItem item) {
		if(id==2) second=item;
		else first=item;
	}

	public LinkedItem getLink(int id) {
		if(id==2) return second;
		if(id==1) return first;
		return null;
	}

}
