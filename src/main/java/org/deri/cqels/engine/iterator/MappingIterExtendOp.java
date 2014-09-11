package org.deri.cqels.engine.iterator;

import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.ExprException;
import org.deri.cqels.data.ExtendMapping;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.ExecContext;
import org.openjena.atlas.logging.Log;

/**
 *
 * @author Michael Jacoby <michael.jacoby@student.kit.edu>
 */
public class MappingIterExtendOp extends MappingIterProcessBinding {

    VarExprList exprs;

    public MappingIterExtendOp(MappingIterator mIter, ExecContext context, VarExprList exprs) {
        super(mIter, context);
        this.exprs = exprs;
    }

    @Override
    public Mapping accept(Mapping mapping) {
        try {
            return new ExtendMapping(context, mapping, exprs);
        } catch (ExprException ex) {
            return null;
        } catch (Exception ex) {
            Log.warn(this, "General exception in " + exprs, ex);
            return null;
        }
    }

}
