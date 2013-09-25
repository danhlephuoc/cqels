package org.deri.cqels.engine.windows;

public class OneIdx extends StreamItem{
	public StreamItem prv1;
	//public Entry bl1;
	public OneIdx(){
		super();
	}
	public void load(WindowIndex[] domains,long[] vals){
		this.vals=vals;
		Entry entry=(Entry)domains[0].get(vals[0]);
		if(entry!=null&&entry.count>0){
			entry.count++;
			prv1=entry.eor;
		}else{
			entry=domains[0].putFirstEntry(vals[0],entry);
			//entry.bor=this;
			prv1=null;
		}	
		entry.eor=this;
	}

	@Override
	public StreamItem getPrv(byte idx) {
		if(idx==0) return prv1;
		return null;
	}
	
	@Override
	public void invalidate(WindowIndex[] indexes) {
		Entry entry=indexes[0].get(vals[0]);
		if(entry==null) return ;
		if(entry.count==1){
			entry.count=0;
			indexes[0].remove(vals[0]);
			entry.eor=null;
		}
		else{
			entry.count--;
		}
	}
}
