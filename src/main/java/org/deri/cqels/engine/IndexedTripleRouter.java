package org.deri.cqels.engine;

import com.espertech.esper.client.EPStatement;
import java.util.HashMap;

import org.deri.cqels.data.EnQuad;
import org.deri.cqels.data.HashMapping;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.iterator.MappingIterator;
import org.deri.cqels.lang.cqels.OpStream;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * This class implements router that it data buffer is a window
 *
 * @author	Danh Le Phuoc
 * @author Chan Le Van
 * @organization DERI Galway, NUIG, Ireland www.deri.ie
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email danh.lephuoc@deri.org
 * @email chan.levan@deri.org
 * @email michael.jacoby@student.kit.edu
 * @see OpRouterBase
 */
public class IndexedTripleRouter extends OpRouterBase {

    public static long accT = 0;
    private final Quad quad;
    private final Window w;
    private final IndexedOnWindowBuff buff;
    private final EPStatement statement;

    /**
     * @param ctx execution context
     * @param op triple
     * @param graph
     * @param w window
     */
    public IndexedTripleRouter(ExecContext ctx, OpTriple op, Node graph, Window w) {
        super(ctx, op);
        quad = new Quad(graph, op.getTriple());
        buff = new IndexedOnWindowBuff(ctx, quad, this, w);
        this.w = w;
        statement = context.engine().addWindow(quad, ".win:length(1)");
        statement.setSubscriber(this);
    }

    /**
     * @param ctx execution context
     * @param op stream
     */
    public IndexedTripleRouter(ExecContext ctx, OpStream op) {
        super(ctx, op);
        quad = new Quad(op.getGraphNode(), op.getBasicPattern().get(0));
        this.w = op.getWindow().clone();
        buff = new IndexedOnWindowBuff(ctx, quad, this, w);
        statement = context.engine().addWindow(quad, ".win:length(1)");
        statement.setSubscriber(this);
    }

    public void stop() {
        statement.setSubscriber(null);
        statement.stop();
        statement.destroy();
    }

    /**
     * This method acts as a listener to catch data from Esper engine
     *
     * @param enQuad the quad coming in
     */
    public void update(EnQuad enQuad) {
        //Add to indexed buff
        w.purge();
        buff.add(enQuad);
        //forward to upper operator
        //System.out.println(context.op2Id(getOp())+" s "+enQuad.getSID() +"-"+enQuad.getOID());
        HashMap<Var, Long> hMap = new HashMap<Var, Long>();
        if (quad.getGraph().isVariable()) {
            hMap.put((Var) quad.getGraph(), enQuad.getGID());
        }
        if (quad.getSubject().isVariable()) {
            hMap.put((Var) quad.getSubject(), enQuad.getSID());
        }
        if (quad.getPredicate().isVariable()) {
            hMap.put((Var) quad.getPredicate(), enQuad.getPID());
        }
        if (quad.getObject().isVariable()) {
            hMap.put((Var) quad.getObject(), enQuad.getOID());
        }
        //System.out.println("received");
        _route(new HashMapping(context, hMap));
    }

    @Override
    public MappingIterator searchBuff4Match(Mapping mapping) {
        //buff.purge(window);
        return buff.search4MatchMapping(mapping);
    }

    public MappingIterator getBuff() {
        //buff.purge(window);
        return buff.iterator();
    }

    public void visit(RouterVisitor rv) {
        rv.visit(this);
    }

    public Window getWindow() {
        return this.w;
    }

}
