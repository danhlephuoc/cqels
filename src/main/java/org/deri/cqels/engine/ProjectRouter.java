package org.deri.cqels.engine;

import java.util.List;

import org.deri.cqels.data.Mapping;
import org.deri.cqels.data.ProjectMapping;
import org.deri.cqels.engine.iterator.MappingIterator;

import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterProject;
import org.deri.cqels.engine.iterator.MappingIterOnQueryIter;
import org.deri.cqels.engine.iterator.QueryIterOnMappingIter;

/**
 * This class implements the router with project operator
 *
 * @author	Danh Le Phuoc
 * @author Chan Le Van
 * @organization DERI Galway, NUIG, Ireland www.deri.ie
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email danh.lephuoc@deri.org
 * @email chan.levan@deri.org
 * @email michael.jacoby@student.kit.edu
 * @see OpRouter1
 */
public class ProjectRouter extends OpRouter1 {

    List<Var> vars;

    public ProjectRouter(ExecContext context, OpProject op, OpRouter sub) {
        super(context, op, sub);
        vars = op.getVars();
    }

    @Override
    public void route(Mapping mapping) {
        //System.out.println("project "+mapping);
        _route(new ProjectMapping(context, mapping, vars));
    }

    @Override
    public MappingIterator searchBuff4Match(Mapping mapping) {
        //TODO: check if necessary to call this method
        QueryIterProject projectItrGroup = new QueryIterProject(new QueryIterOnMappingIter(context, sub().searchBuff4Match(mapping)), vars, context.getARQExCtx());
        return new MappingIterOnQueryIter(context, projectItrGroup);
    }

    @Override
    public MappingIterator getBuff() {
        QueryIterProject projectItrGroup = new QueryIterProject(new QueryIterOnMappingIter(context, sub().getBuff()), vars, context.getARQExCtx());
        return new MappingIterOnQueryIter(context, projectItrGroup);
    }

    public void visit(RouterVisitor rv) {
        rv.visit(this);
        this.subRouter.visit(rv);
    }
}
