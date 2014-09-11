package org.deri.cqels.engine;

import java.lang.reflect.Method;
import java.util.List;

import org.deri.cqels.data.Mapping;
import org.deri.cqels.data.MappingWrapped;
import org.deri.cqels.engine.iterator.MappingIterator;

public class ThroughRouter extends OpRouterBase {

    List<? extends OpRouter> dataflows;

    public ThroughRouter(ExecContext context, List<OpRouter> dataflows) {
        super(context, dataflows.get(0).getOp());
        this.dataflows = dataflows;
    }

    @Override
    public void route(Mapping mapping) {
        _route(new MappingWrapped(context, mapping));
    }

    @Override
    public MappingIterator searchBuff4Match(Mapping mapping) {
        return dataflows.get(0).searchBuff4Match(mapping);
    }

    @Override
    public MappingIterator getBuff() {
        return dataflows.get(0).getBuff();
    }

    public void visit(RouterVisitor rv) {
        rv.visit(this);
        OpRouter router = dataflows.get(0);
        try {
            Method method = router.getClass().getMethod("visit", RouterVisitor.class);
            method.invoke(router, rv);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
