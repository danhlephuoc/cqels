package org.deri.cqels.engine.windows;

public class TwoIdxes extends StreamItem{
	
	public StreamItem prv1,prv2;
	
	public TwoIdxes(){
		super();
	}
	
	public void load(WindowIndex[] domains,long[] vals){
		this.vals=vals;
		Entry entry1=(Entry)domains[0].get(vals[0]);
		Entry entry2=(Entry)domains[1].get(vals[1]);
		if(entry1!=null){
			entry1.count++;
			prv1=entry1.eor;
			
		}else{			
			domains[0].putFirstEntry(vals[0],entry1);
			prv1=null;
		}	
		entry1.eor=this;
		if(entry2!=null){
			entry2.count++;
			prv2=entry2.eor;
			
		}else{			
			domains[1].putFirstEntry(vals[1], entry2);
			prv2=null;
		}	
		entry2.eor=this;
	}
	
	
	@Override
	public StreamItem getPrv(byte idx) {
		if(idx==0) return prv1;
		if(idx==1) return prv2;
		return null;
	}

	@Override
	public void invalidate(WindowIndex[] indexes) {
		for(int i=0;i<2;i++){
			
			Entry entry=indexes[i].get(vals[i]);
			if(entry==null) continue;
			if(entry.count==1){
				entry.count=0;
				indexes[i].remove(vals[i]);
				entry.eor=null;
			}
			else{
				entry.count--;
			}
		}
	}
}
