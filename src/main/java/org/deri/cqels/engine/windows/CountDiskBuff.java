package org.deri.cqels.engine.windows;

import com.sleepycat.je.Environment;


public class CountDiskBuff extends DiskBuff {
	long count;
	public CountDiskBuff(Environment env,byte cols,byte idxs,int count) {
		super(env,cols,idxs);
		this.count=count;
		init();
	}
	
	public CountDiskBuff(Environment env,byte cols,byte idxs,int count,int segment) {
		super(env,cols,idxs,segment);
		this.count=count;
		init();
	}
	
	public void purge(){
		while(size()>count){
			System.out.println("size "+size() +" count "+count);
			if(remove()==null){
				System.out.println("break");
				break;			
			}
		}
	}


	@Override
	public SubWindowBuff initWin(WindowIndex[] domains) {
		// TODO Auto-generated method stub
		return new CountSubBuff(this, (int)count);
	}
}
