package org.deri.cqels.engine;

import org.deri.cqels.lang.cqels.Duration;
import org.deri.cqels.lang.cqels.DurationSet;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
/** 
 * This class implements the time-based window 
 * @author		Danh Le Phuoc
 * @author 		Chan Le Van
 * @organization DERI Galway, NUIG, Ireland  www.deri.ie
 * @email 	danh.lephuoc@deri.org
 * @email   chan.levan@deri.org
 */
public class RangeWindow implements Window {
	Database buff;
    long w;
    long slide = 1;
    long lastTimestamp = -1;
 
	public RangeWindow( long w) {
    	this.w = w;
    }
	
	public RangeWindow(DurationSet durations, Duration slideDuration) {
    	this.w = durations.inNanoSec();
    	if(slideDuration != null) {
    		slide = slideDuration.inNanosec();
    	}
    }
	
	public void setBuff(Database db) { 
		buff = db;
	}
	
	public void purge() {
        //System.out.println("call purge");
		long curTime = System.nanoTime();
		if(lastTimestamp > 0 && lastTimestamp < curTime - w) {
			Cursor cursor = buff.openCursor(null, CursorConfig.DEFAULT);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			long tmp;
			while(cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				tmp = LongBinding.entryToLong(key);
				if(tmp < curTime - w) {
					//System.out.println("actually purge");
					cursor.delete();
					//System.out.println("purge" +lastTimestamp + " cur"+curTime+ " "+(curTime-w));
				}
				else {
					cursor.close();
					lastTimestamp = tmp;
					report(curTime);
					return;
				}
			}
			cursor.close();
			lastTimestamp = -1;
			
		}
		report(curTime);
	}
	public void reportLatestTime(long t) {
		if(lastTimestamp < 0) {
			lastTimestamp = t;
		}
	}
	
	public void report(long t){
		IndexedTripleRouter.accT += System.nanoTime() - t;
	}
}
