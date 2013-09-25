package org.deri.cqels.engine.winbuff;

import it.unimi.dsi.fastutil.HashCommon;

import java.util.HashMap;
import java.util.Iterator;

import org.deri.cqels.engine.opimpl.POOL;

import com.hp.hpl.jena.sparql.core.Var;

public class MUN extends TimestampedMapping {
	HashMap<Var, Integer> map;
    long[] vals;
    public MUN(){};
    LinkedItem link;
	public void set(long[] vals,HashMap<Var, Integer> map){
		this.map=map;
		if(this.vals!=null)
			POOL.arrayFactory(this.vals.length).returnObject(this.vals);
		this.vals=vals;
	}
	public long get(Var var) {
		if(map.get(var)!=null)
		return vals[map.get(var)];
		return -1;
	}
	public boolean checkExpired() {
		return timestamp<0;
	}
	public Iterator<Var> vars() {
		return map.keySet().iterator();
	}
	
	@Override
	public int hashCode() {
		return (int)HashCommon.murmurHash3(Math.abs(timestamp));
	}
	
	@Override
	public boolean equals(Object obj) {
			if(obj instanceof MUN)
				return Math.abs(timestamp)==Math.abs(((MUN)obj).timestamp);
			return super.equals(obj);
	}
	
	public LinkedItem getLink() {
		return link;
	}
	public void setLink(LinkedItem item) {
		this.link=item;
	}
	public PoolableObject newObject() {
		// TODO Auto-generated method stub
		return (PoolableObject) POOL.MUN.borrowObject();
	}
	public void releaseInstance() {
		POOL.MUN.returnObject(this);		
	}	
}
