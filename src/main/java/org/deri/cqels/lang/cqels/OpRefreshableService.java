package org.deri.cqels.lang.cqels;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpService;

/**
 *
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class OpRefreshableService extends OpService implements OpRefreshable {

    private final DurationSet duration;

    public OpRefreshableService(Node serviceNode, Op subOp, boolean silent, DurationSet duration) {
        super(serviceNode, subOp, silent);
        this.duration = duration;
    }

    public DurationSet getDuration() {
        return duration;
    }

    @Override
    public Op copy(Op newOp) {
        return new OpRefreshableService(getService(), newOp, getSilent(), duration);
    }
}
