package org.deri.cqels.engine;

import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import org.deri.cqels.data.HashMapping;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.iterator.MappingIterMatch;
import org.deri.cqels.engine.iterator.MappingIterator;
import org.deri.cqels.engine.iterator.MappingNestedLoopEqJoin;

/**
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class UnionRouter extends OpRouter2 {

    public UnionRouter(ExecContext context, OpUnion op, OpRouter left, OpRouter right) {
        super(context, op, left, right);
    }

    public void route(Mapping mapping) {
        // combine mappings from left and right??
        OpRouter childR = getOtherChildRouter(mapping);
        MappingIterator itr = childR.getBuff();
        while (itr.hasNext()) {
            Mapping _mapping = itr.next();
            Mapping temp = new HashMapping(context, _mapping, mapping);
            _route(temp);
        }
        itr.close();
        //System.out.println("end mapping ");
    }

    public OpRouter getOtherChildRouter(Mapping mapping) {
        if (left().getId() == mapping.from().getId()) {
            return right();
        }
        return left();
    }

    public MappingIterator getBuff() {
        OpRouter lR = left();
        OpRouter rR = right();
        //TODO: choose sort-merge join order
        if (lR instanceof IndexedTripleRouter) {
            new MappingNestedLoopEqJoin(context, rR, lR);
        }
        return new MappingNestedLoopEqJoin(context, lR, rR);
    }

    @Override
    public MappingIterator searchBuff4Match(Mapping mapping) {
        return new MappingIterMatch(context, getBuff(), mapping);
    }

    public void visit(RouterVisitor rv) {
        rv.visit(this);
        left().visit(rv);
        right().visit(rv);
    }

}
