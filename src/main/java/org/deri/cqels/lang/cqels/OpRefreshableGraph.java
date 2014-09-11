package org.deri.cqels.lang.cqels;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;

/**
 *
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class OpRefreshableGraph extends OpGraph implements OpRefreshable {

    private final DurationSet duration;

    public OpRefreshableGraph(Node node, Op pattern, DurationSet duration) {
        super(node, pattern);
        this.duration = duration;
    }

    public DurationSet getDuration() {
        return duration;
    }

    @Override
    public Op copy(Op newOp) {
        return new OpRefreshableGraph(getNode(), newOp, duration);
    }

}
