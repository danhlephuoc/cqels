package org.deri.cqels.engine.winbuff;

import java.util.ArrayList;
import java.util.Iterator;

import org.deri.cqels.engine.opimpl.PhysicalOp;
import org.deri.cqels.engine.opimpl.POOL;

import com.hp.hpl.jena.sparql.core.Var;

public class MUP extends MUBase {
	MU base;
	ArrayList<Var> projVars;
	DomEntry domEntry;
	
	public void set(PhysicalOp op, MU base,ArrayList<Var> projVars) {
		this.base=base;
		this.projVars=projVars;
		from=op;
	}
	public long get(Var var) {
		if(projVars.contains(var))
			return base.get(var);
		return -1;
	}

	public Iterator<Var> vars() {
		return projVars.iterator();
	}

	public boolean isExpired() {	return base.isExpired(); }

	public LinkedItem getLink(int idx) {
		return domEntry;
	}
	
	public void setLink(DomEntry entry) {
		this.domEntry=entry;
	}
	
	public PoolableObject newObject() {
		// TODO Auto-generated method stub
		return (PoolableObject)POOL.MUP.borrowObject();
	}

	public void releaseInstance() {
		POOL.MUP.returnObject(this);
	}
	@Override
	public void visit(Visistor visitor) {
		visitor.visit(base);
		visitor.visit(this);
	}
	@Override
	protected boolean checkExpired() {		return base.isExpired();
	}
}
