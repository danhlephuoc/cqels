package org.deri.cqels.engine.winbuff;

import java.util.Iterator;

import org.deri.cqels.engine.opimpl.POOL;
import org.deri.cqels.engine.opimpl.PhysicalOp;
import org.openjena.atlas.iterator.IteratorConcat;

import com.hp.hpl.jena.sparql.core.Var;

public  class MUJ extends MUBase {
    MUBase left,right;
    public MUJ(){
    	
    }
    public void set(MUBase left,MUBase right,PhysicalOp from){
    	this.left=left;
    	this.right=right;
    	this.from=from;
    	expired=false;
    }
    
    
	public long get(Var var) {
		long val=left.get(var);
		if(val!=-1) return val;
		return right.get(var);
	}

	public boolean checkExpired() {
		return left.isExpired()||right.isExpired();
	}
	
	public void releaseInstance() {
		if(left.isExpired()) left.releaseInstance();
		if(right.isExpired()) right.releaseInstance();
		release();
	}
	@Override
	public void visit(Visistor visitor) {
		visitor.visit(left);
		visitor.visit(right);
		visitor.visit(this);
	}
	
	protected void release(){
		POOL.MUJ.returnObject(this);
	}
	
	public Iterator<Var> vars() {
		return IteratorConcat.concat(left.vars(), right.vars());
	}

	public PoolableObject newObject() {
		// TODO Auto-generated method stub
		return (PoolableObject)POOL.MUJ.borrowObject();
	}
}
