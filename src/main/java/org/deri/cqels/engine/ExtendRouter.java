package org.deri.cqels.engine;

import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.core.VarExprList;

import org.deri.cqels.data.ExtendMapping;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.iterator.MappingIterExtendOp;
import org.deri.cqels.engine.iterator.MappingIterator;

/**
 *
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class ExtendRouter extends OpRouter1 {

    VarExprList exprs;

    public ExtendRouter(ExecContext context, OpExtend op, OpRouter sub) {
        super(context, op, sub);
        exprs = op.getVarExprList();
    }

    @Override
    public void route(Mapping mapping) {
        //System.out.println("extend "+mapping);
        _route(new ExtendMapping(context, mapping, exprs));
    }

    @Override
    public MappingIterator searchBuff4Match(Mapping mapping) {
        //TODO: check if necessary to call this method
        return new MappingIterExtendOp(sub().searchBuff4Match(mapping), context, exprs);
    }

    @Override
    public MappingIterator getBuff() {
        return new MappingIterExtendOp(sub().getBuff(), context, exprs);
    }

    public void visit(RouterVisitor rv) {
        rv.visit(this);
        this.subRouter.visit(rv);
    }
}
