package org.deri.cqels.engine.windows;

import java.util.Iterator;
import java.util.LinkedList;

import org.openjena.atlas.iterator.IteratorConcat;

import com.sleepycat.je.Environment;

public  abstract class DiskBuff implements WindowBuff {
	byte cols,idxs;
	protected int segment,threshold=2;
	
	OnDiskWindow firstD,lastD,toBeSpilled,toBeLoaded;
	//public WindowIndex[] domains;
	//TODO : is it count? or extended version?
	//RingWindowBuff first,last;
	int mems,subWins;
	
	//boolean loading =false;
	 Environment env;
	 //TODO domains????
	public DiskBuff(Environment env,byte cols,byte idxs){
		this(env,cols,idxs,(int)1E6);
	}
	
	public DiskBuff(Environment env,byte cols,byte idxs,int segment){
		this.cols=cols;
		this.idxs=idxs;
		this.env=env;
		this.segment=segment;
	}
	
	public  void init(){
		mems=1;
		subWins=1;
		lastD=new OnDiskWindow(env, cols,initWin(domains()),this);
		firstD=lastD;
		firstD.buff.setIgnorePurge(true);
		toBeSpilled=lastD;
	}
	
	public OnDiskWindow newDiskBuff(SubWindowBuff rWin){
		return new OnDiskWindow(env, cols,rWin,this);
	}
	
	public void add(long[] vals) {
		//TODO : devide to subwindows	
		
		if(lastD.size()>=segment){
			OnDiskWindow tmp=lastD;
			lastD=newDiskBuff(initWin(domains()));
			mems++;
			subWins++;
			lastD.prv=tmp;			
			tmp.next=lastD;
			if(mems>threshold&&toBeSpilled!=null){
				toBeSpilled.spill();
				toBeSpilled=toBeSpilled.prv;
				mems--;
			}
		}
		lastD.buff.add(vals);
		purge();
	}
	


	public Iterator<long[]> search(byte i, long val) {
		purge();
		IteratorConcat<long[]> concat=new IteratorConcat<long[]>();
		
		OnDiskWindow block=firstD;
		while(block!=null)
		{
			concat.add(block.search(i, val));
			block=block.next;
		}
		return concat;
	}
	
	public abstract SubWindowBuff initWin(WindowIndex[] domains);
	
	public Iterator<StreamItem> iterator() {
		// TODO is it needed or forbidden?
		return null;
	}
	
	public int size() {
		if(subWins==1)
			return lastD.size();
		if(subWins==2)
			return firstD.size()+lastD.size();
		return (subWins-2)*segment +firstD.size()+lastD.size();
	}
	
	public LinkedList<StreamItem> items() {
		// TODO it's not needed?
		return null;
	}
	
	public WindowIndex[] domains() {	
		WindowIndex[] domains=new WindowIndex[idxs];
		for(int i=0;i<idxs;i++){
			 domains[i]=new DSIOpenHashIndex();
		}
		return domains;
    }
	
	public StreamItem remove() { 
		System.out.println("DiskBuff.remove "+firstD.size());
		if(firstD.size()==0){
			//TODO: check if it's already loaded???
			System.out.println("move to another subwindow");
			firstD=firstD.next;
			subWins--;
			
			if(firstD!=null){
				firstD.buff.setIgnorePurge(false); 
				if(firstD.next!=null)
					firstD.next.reload();
			}
		}
		return firstD.remove();
	}
	
}
