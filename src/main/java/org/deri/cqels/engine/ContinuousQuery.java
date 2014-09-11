package org.deri.cqels.engine;

import com.hp.hpl.jena.query.Query;
import java.util.ArrayList;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.ContinuousListener;
import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.engine.OpRouter;
import org.deri.cqels.engine.OpRouter1;
import org.deri.cqels.engine.RouterVisitor;

/**
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public abstract class ContinuousQuery<T extends ContinuousListener> extends OpRouter1 {

    protected Query query;
    protected ArrayList<T> listeners;
    protected boolean isSelect = false;
    protected boolean isConstruct = false;

    public ContinuousQuery(ExecContext context, Query query, OpRouter subRouter) {
        super(context, subRouter.getOp(), subRouter);
        this.query = query;
        listeners = new ArrayList<T>();
    }

    @Override
    public void route(Mapping mapping) {
        for (T lit : listeners) {
            lit.update(mapping);
        }
    }

    public void register(T lit) {
        listeners.add(lit);
    }

    public void unregister(T lit) {
        listeners.remove(lit);
    }

    public void visit(RouterVisitor rv) {
        rv.visit(this);
        this.subRouter.visit(rv);
    }

    public Query getQuery() {
        return query;
    }

    /**
     * @return the isSelect
     */
    public boolean isSelect() {
        return isSelect;
    }

    /**
     * @return the isConstruct
     */
    public boolean isConstruct() {
        return isConstruct;
    }
}
