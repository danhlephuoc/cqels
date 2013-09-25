package org.deri.cqels.engine.winbuff;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.core.Var;

public interface MU extends LinkedItem,RoutingMessage{
	
	public long get(Var var);
	public Iterator<Var> vars();
	public boolean isExpired();
	public void visit(Visistor visitor);
}
