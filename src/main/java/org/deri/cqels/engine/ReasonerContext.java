package org.deri.cqels.engine;

public class ReasonerContext extends ExecContext {
	Reasoner reasoner;
	public ReasonerContext(String path, boolean cleanDataset, Reasoner reasoner) {
		super(path, cleanDataset);
		this.reasoner = reasoner;
		///TODO
	}
	
	@Override 
	public void loadDataSet() {
		///TODO with reasoner
	}
	
	@Override
	public void loadDefaultDataset() {
		///TODO with reasoner
	}
}
