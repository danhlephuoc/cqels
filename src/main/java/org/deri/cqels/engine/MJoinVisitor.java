package org.deri.cqels.engine;

import org.deri.cqels.engine.opimpl.MJoin;
import org.deri.cqels.engine.winbuff.AWB;

public class MJoinVisitor implements RouterVisitor {

	AWB[] windows;
	MJoin mJoin;
	
	public MJoinVisitor() {
		
	}
	
	
	public void visit(JoinRouter router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(IndexedTripleRouter router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ProjectRouter router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ThroughRouter router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(BDBGraphPatternRouter router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ExtendRouter router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(FilterExprRouter router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ContinuousConstruct router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ContinuousSelect router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(GroupRouter router) {
		// TODO Auto-generated method stub
		
	}

	public void visit(OpRouter router) {
		// TODO Auto-generated method stub
		
	}

}
