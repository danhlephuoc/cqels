package org.deri.cqels.engine.windows;

import java.util.ArrayDeque;

public class ArrayDequeStreamItemPool extends StreamItemPool {
	ArrayDeque<StreamItem> queue;
	public ArrayDequeStreamItemPool(){
		queue=new ArrayDeque<StreamItem>();
	}
	@Override
	public void add(StreamItem itm) { queue.add(itm);}

	@Override
	public void add(StreamItemPool other) {
		// TODO add items from other poll

	}

	@Override
	public StreamItem first() {
		// TODO Auto-generated method stub
		return queue.getFirst();
	}

	@Override
	public StreamItem last() {
		// TODO Auto-generated method stub
		return queue.getLast();
	}

	@Override
	public StreamItem poll() {
		// TODO Auto-generated method stub
		return queue.poll();
	}

}
