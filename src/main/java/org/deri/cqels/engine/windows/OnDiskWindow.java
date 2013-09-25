package org.deri.cqels.engine.windows;

import java.util.Iterator;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class OnDiskWindow{
		
	public Database valueDB;
	public Database[] idxDBs;
	public OnDiskWindow next,prv;
	byte cols;
	Environment env;
	public SubWindowBuff buff;
	boolean spilling =true;
	DiskBuff disk;
	public OnDiskWindow(Environment env,byte cols,SubWindowBuff buff,DiskBuff disk){
		long id=System.nanoTime();
		this.env=env;
		this.cols=cols;
		this.buff=buff;
		this.disk=disk;
		valueDB=initDB(id+"_value");
		idxDBs=new Database[buff.domains().length];
		for(int i=0;i<buff.domains().length;i++)
			idxDBs[i]=initDB(id+"_idx_"+i);
		spill();
	}
	
	public void removeDB(){
		Utils.deleteDB(valueDB);
		for(Database db:idxDBs)
			Utils.deleteDB(db);
	}
	public Database initDB(String dbName){
		DatabaseConfig dbConfig=new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setTemporary(false);
		Database db=env.openDatabase(null, dbName,dbConfig);
		return db;
	}
	
	public Iterator<long[]> search(byte i,long key) {
		//TODO: materialize the iterator
		if(spilling)
			return buff.search(i, key);
		//System.out.println(idxDBs[i].count());
		return new StreamItr(idxDBs[i],key);
	}
	
	public RingWindowBuff loadSubWin(Database db,WindowIndex[] domains){
		buff=disk.initWin(domains);
		CursorConfig config=new CursorConfig();
		Cursor cursor = null;
		try {		
		    cursor = db.openCursor(null, config);
		   
		    DatabaseEntry foundKey = new DatabaseEntry();
		    DatabaseEntry foundData = new DatabaseEntry();
		   
		    while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
		        OperationStatus.SUCCESS) {
		        long[] vals= new long[cols];
		        long time=LongBinding.entryToLong(foundKey);
		        TupleInput input=new TupleInput(foundData.getData());
		        for(int i=0;i<cols;i++)
		        	vals[i]=input.readLong();
		        buff.add(time,vals);
		    }
		} catch (DatabaseException de) {
		    System.err.println("Error accessing database." + de);
		} finally {
		    cursor.close();
		}
		
		return buff;
	}
	
	public void valueFlush(Database db){
		for(Iterator<StreamItem>itr=buff.iterator();itr.hasNext();){
			StreamItem itm=itr.next();
			DatabaseEntry key=new DatabaseEntry();
			LongBinding.longToEntry(itm.time, key);
			TupleOutput out=new TupleOutput();

			for(int i=0;i<itm.vals.length;i++)
				out.writeLong(itm.vals[i]);
			DatabaseEntry data=new DatabaseEntry(out.getBufferBytes());
			db.put(null, key, data);
		}
	}
	
	public void indexFlush(Database db,byte idx){
		for(Iterator<Long>itr=buff.domains()[idx].keys();itr.hasNext();){
			long k=itr.next();
			DatabaseEntry key=new DatabaseEntry();
			LongBinding.longToEntry(k, key);

			TupleOutput out=new TupleOutput();
			for(Iterator<long[]> itrS=buff.search(idx,k);itrS.hasNext();){
				long[] vals=itrS.next();
				if(idx==0)out.writeLong(vals[1]);
				else out.writeLong(vals[0]);
			}
			
			DatabaseEntry data=new DatabaseEntry(out.getBufferBytes());
			db.put(null, key, data);
		}
	}
	
	public void reload(){
		//allow only one thread for reload
		new Thread(){
				public void run(){
					loadSubWin(valueDB, disk.domains());
					//sync();
				}
			}.start();
	}
	public void spill(){
		//allow only one thread for spiling
		//if(spilling){
			new Thread(){
				public void run(){
					//TODO : initDB for spilling
					valueFlush(valueDB);
					for(byte i=0;i<buff.domains().length;i++)
						indexFlush(idxDBs[i], i);
					spilling=false;
					buff.release();
				}
				
			}.start();
		//}
	}
	
	public int size(){
		if(buff!=null) return buff.size();
		return (int)valueDB.count();
	}
	
	public StreamItem remove(){
		System.out.println("OnDiskWindow.remove");
		if(buff!=null)
			return buff.remove();
		System.out.println("buff null");
		return null;
	}
}
