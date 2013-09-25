package org.deri.cqels.engine.winbuff;

import it.unimi.dsi.fastutil.HashCommon;

import java.util.Iterator;

import org.deri.cqels.engine.opimpl.PhysicalOp;

import com.hp.hpl.jena.sparql.core.Var;

public abstract class MUBase implements MU,PoolableObject,LinkedItem {
	protected PhysicalOp from;
	boolean expired;
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MU){
			Var var;
			for(Iterator<Var>itr=vars();itr.hasNext();){
				var=itr.next();
				if(((MU)obj).get(var)!=get(var)) return false;
			}
			return true;
		}
		return false;
	}
	
	public PhysicalOp from() {		return from; }	
	
	public void setFrom(PhysicalOp from){ this.from=from;}
	
	@Override
	public int hashCode() {
		int hashCode=0;
		for(Iterator<Var>itr=vars();itr.hasNext();)
			hashCode=(int)(31*hashCode+HashCommon.murmurHash3(get(itr.next())));			
		return hashCode;
	}
	
	public boolean isExpired(){
		return expired||checkExpired();
	}
	protected abstract boolean checkExpired();
	
	public void visit(Visistor visitor) {
		visitor.visit(this);
	}
	
	public LinkedItem getLink() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setLink(LinkedItem item) {
		// TODO Auto-generated method stub	
	}
}
