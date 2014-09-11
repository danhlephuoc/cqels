package org.deri.cqels.engine;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpVisitorByType;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.TransformBase;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpN;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.lang.cqels.DurationSet;
import org.deri.cqels.lang.cqels.ElementStreamGraph;
import org.deri.cqels.lang.cqels.OpRefreshable;
import org.deri.cqels.lang.cqels.OpRefreshableGraph;
import org.deri.cqels.lang.cqels.OpRefreshableService;
import org.deri.cqels.lang.cqels.OpRefreshableStreamGraph;
import org.deri.cqels.lang.cqels.OpStream;
import org.openjena.atlas.lib.SetUtils;

/**
 * This class uses heuristic approach to build an execution plan
 *
 * @author	Danh Le Phuoc
 * @author Chan Le Van
 * @organization DERI Galway, NUIG, Ireland www.deri.ie
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email danh.lephuoc@deri.org
 * @email chan.levan@deri.org
 * @email michael.jacoby@student.kit.edu
 */
public class HeuristicRoutingPolicy extends RoutingPolicyBase {

    public HeuristicRoutingPolicy(ExecContext context) {
        super(context);
        this.compiler = new LogicCompiler();
        this.compiler.set(this);
    }

    /**
     * Creating the policy to route the mapping data
     *
     * @param query
     * @return a router representing a tree of operators
     */
    @Override
    public OpRouter generateRoutingPolicy(Query query) {
        ElementGroup group = new ElementGroup();
        if (query.getQueryPattern() instanceof ElementGroup) {
            group = (ElementGroup) query.getQueryPattern();
        } else if (query.getQueryPattern() instanceof Element) {
            group.addElement((Element) query.getQueryPattern());
        }

        final List<ElementFilter> filters = new ArrayList<ElementFilter>();
        final List<Op> streamOps = new ArrayList<Op>();
        final List<Op> others = new ArrayList<Op>();
        final List<OpRouter> subQueries = new ArrayList<OpRouter>();
        final Transform updateRefreshIntervalsTransform = new UpdateRefreshIntervalsTransformer(compiler.compile(query));

        for (Element el : group.getElements()) {
            if (el instanceof ElementFilter) {
                filters.add((ElementFilter) el);
                continue;
            }
            if (el instanceof ElementStreamGraph) {
                addStreamOp(streamOps, (ElementStreamGraph) el);
                continue;
            }
            if (el instanceof ElementSubQuery) {
                subQueries.add(generateRoutingPolicy(((ElementSubQuery) el).getQuery()));
                ElementWalker.walk(((ElementSubQuery) el).getQuery().getQueryPattern(), new ElementVisitorBase() {
                    @Override
                    public void visit(ElementNamedGraph el) {
                        if (el instanceof ElementStreamGraph) {
                            streamOps.add(subQueries.get(subQueries.size() - 1).getOp());
                        }
                    }
                });
                continue;
            }
            others.add(Transformer.transform(updateRefreshIntervalsTransform, compiler.compile(el)));
        }

        /* push the filter down to operators on RDF datasets */
        for (int i = 0; i < others.size(); i++) {
            Op op = others.get(i);
            for (ElementFilter filter : filters) {
                if (OpVars.allVars(op).containsAll(filter.getExpr().getVarsMentioned())) {
                    op = OpFilter.filter(filter.getExpr(), op);
                }
            }
            others.set(i, op);
        }

        /*project the necessary variables */
        project(filters, streamOps, others, query);

        /* Initialize query execution context, download named graph, create cache?.... */
        for (String uri : query.getNamedGraphURIs()) {
            if (!context.getDataset().containsGraph(Node.createURI(uri))) {
                System.out.println(" load" + uri);
                context.loadDataset(uri, uri);
            }
        }

        /* create Leaf cache from the operator over RDF datasets */
        List<OpRouter> caches = new ArrayList<OpRouter>();
        for (Op op : others) {
            if (checkContainsInstacesOf(op, OpRefreshable.class)) {
                caches.add(new RefreshableBDBGraphPatternRouter(context, op));
            } else {
                caches.add(new BDBGraphPatternRouter(context, op));
            }
        }

        /* create router for window operators */
        List<OpRouter> windows = new ArrayList<OpRouter>();
        for (Op op : streamOps) {
            if (op instanceof OpStream) {
                windows.add(new IndexedTripleRouter(context, (OpStream) op));
            }
        }
        for (OpRouter subQuery : subQueries) {
            if (checkContainsInstacesOf(subQuery.getOp(), OpStream.class)) {
                // treat as stream operator from now on
                windows.add(subQuery);
            } else {
                caches.add(subQuery);
            }
        }

        /*
         * create routing plan for each window operator
         * how to route ???? hash operator is not unique
         */
        int i = 0;
        BitSet globalCFlag = new BitSet(others.size());
        List<OpRouter> dataflows = new ArrayList<OpRouter>();
        for (OpRouter router : windows) {
            BitSet wFlag = new BitSet(windows.size());
            wFlag.set(i++);
            BitSet cFlag = new BitSet(others.size());
            BitSet fFlag = new BitSet(filters.size());

            Op curOp = router.getOp();
            OpRouter curRouter = router;
            Set<Var> curVars = (Set<Var>) OpVars.allVars(curOp);
            int count = 1;
            int curCount = 1;

            while (wFlag.size() + cFlag.size() + fFlag.size() > count) {
                curCount = count;
                boolean skip = false;
                for (int j = 0; j < filters.size(); j++) {
                    if ((!fFlag.get(j)) && curVars.containsAll(
                            filters.get(j).getExpr().getVarsMentioned())) {
                        curOp = OpFilter.filter(filters.get(j).getExpr(), curOp);
                        OpRouter newRouter = new FilterExprRouter(context, (OpFilter) curOp, curRouter);
                        curRouter = addRouter(curRouter, newRouter);
                        fFlag.set(j);
                        count++;
                    }
                }

                for (int j = 0; j < caches.size() && (!skip); j++) {
                    if ((!cFlag.get(j)) && SetUtils.intersectionP(curVars,
                            (Set<Var>) OpVars.allVars(caches.get(j).getOp()))) {
                        curOp = OpJoin.create(curOp, caches.get(j).getOp());
                        OpRouter newRouter = new JoinRouter(context, (OpJoin) curOp,
                                curRouter, caches.get(j));
                        curRouter = addRouter(curRouter, newRouter);
                        if (caches.get(j) instanceof RefreshableBDBGraphPatternRouter && !globalCFlag.get(j)) {
                            // not linked in any dataflow -> add routing information
                            next.put(caches.get(j).getId(), newRouter);
                        }
                        cFlag.set(j);
                        globalCFlag.set(j);
                        curVars.addAll(OpVars.allVars(caches.get(j).getOp()));
                        count++;
                        skip = true;
                    }
                }

                for (int j = 0; j < windows.size() && (!skip); j++) {
                    if ((!wFlag.get(j)) && SetUtils.intersectionP(curVars,
                            (Set<Var>) OpVars.allVars(windows.get(j).getOp()))) {
                        curOp = OpJoin.create(curOp, windows.get(j).getOp());
                        OpRouter newRouter = new JoinRouter(context, (OpJoin) curOp,
                                curRouter, windows.get(j));
                        curRouter = addRouter(curRouter, newRouter);
                        wFlag.set(j);
                        curVars.addAll(OpVars.allVars(windows.get(j).getOp()));
                        count++;
                        skip = true;
                    }
                }
                if (curCount == count) {
                    break;
                }
            }
            dataflows.add(curRouter);
        }

        ThroughRouter througRouter = new ThroughRouter(context, dataflows);
        for (OpRouter r : dataflows) {
            next.put(r.getId(), througRouter);
        }
        OpRouter result = througRouter;
        if (globalCFlag.cardinality() < caches.size()) {
            // not all caches got used in dataflows so just add them above the through router
            List<OpRouter> unusedCaches = new ArrayList<OpRouter>();
            for (i = 0; i < caches.size(); i++) {
                if (globalCFlag.get(i)) {
                    continue;
                }
                unusedCaches.add(caches.get(i));
            }
            for (i = 0; i < unusedCaches.size(); i++) {
                Op newOp;
                OpRouter newRouter;
                if (SetUtils.intersectionP((Set<Var>) OpVars.allVars(result.getOp()), (Set<Var>) OpVars.allVars(unusedCaches.get(i).getOp()))) {
                    newOp = OpJoin.create(result.getOp(), unusedCaches.get(i).getOp());
                    newRouter = new JoinRouter(context, (OpJoin) newOp, result, unusedCaches.get(i));
                } else {
                    newOp = OpUnion.create(result.getOp(), unusedCaches.get(i).getOp());
                    newRouter = new UnionRouter(context, (OpUnion) newOp, result, unusedCaches.get(i));
                }
                // if cache is refreshable -> add routing info to next upper level
                if (unusedCaches.get(i) instanceof RefreshableBDBGraphPatternRouter) {
                    next.put(unusedCaches.get(i).getId(), newRouter);
                }
                result = addRouter(result, newRouter);
            }
        }
        //put all into routing policy
        return compiler.compileModifiers(query, througRouter);
    }

    private boolean checkContainsInstacesOf(Op op, Class... classes) {
        class CheckContainsRefreshableOpVisitor extends OpVisitorByType {

            public boolean containsRefreshable = false;
            private final Class[] classes;

            public CheckContainsRefreshableOpVisitor(Class[] classes) {
                this.classes = classes;
            }

            @Override
            protected void visitN(OpN op) {
                checkIfRefreshable(op);
            }

            @Override
            protected void visit2(Op2 op) {
                checkIfRefreshable(op);
            }

            @Override
            protected void visit1(Op1 op) {
                checkIfRefreshable(op);
            }

            @Override
            protected void visit0(Op0 op) {
                checkIfRefreshable(op);
            }

            @Override
            protected void visitExt(OpExt op) {
                checkIfRefreshable(op);
            }

            private void checkIfRefreshable(Op op) {
                for (Class clazz : classes) {
                    if (clazz.isInstance(op)) {
                        containsRefreshable = true;
                    }
                }
            }
        }
        CheckContainsRefreshableOpVisitor visitor = new CheckContainsRefreshableOpVisitor(classes);
        OpWalker.walk(op, visitor);
        return visitor.containsRefreshable;
    }

    /**
     * create the relationship between 2 router nodes. This version uses hash
     * table to identify which router will be the next for the mapping to go
     *
     * @param from the departing router
     * @param newRouter the arriving router
     * @return the arriving router
     */
    @Override
    public OpRouter addRouter(OpRouter from, OpRouter newRouter) {
        next.put(from.getId(), newRouter);
        return newRouter;
    }

    /**
     * get the next router from the current router
     *
     * @param curRouter current router
     * @param mapping
     * @return the next router
     */
    public OpRouter next(OpRouter curRouter, Mapping mapping) {
        //System.out.println("next"+next.get(curRouter.getId()));
        if (next.containsKey(curRouter.getId())) {
            return next.get(curRouter.getId());
        }
        return null;
    }

    /**
     * register the select-type query with the engine
     *
     * @param query
     * @return
     */
    public ContinuousSelect registerSelectQuery(Query query) {
        OpRouter qR = generateRoutingPolicy(query);
        if (query.isSelectType()) {
            ///TODO
            ContinuousSelect rootRouter = (ContinuousSelect) addRouter(qR,
                    new ContinuousSelect(context, query, qR));
            rootRouter.visit(new TimerVisitor());
            return rootRouter;
        }
        return null;
    }

    public void unregisterConstructQuery(ContinuousConstruct constructQuery) {
        unregisterQuery(constructQuery);
    }

    private void unregisterQuery(OpRouter rootRouter) {
        rootRouter.visit(new RouterVisitorBase() {
            public void visit(OpRouter router) {
                if (next.containsKey(router.getId())) {
                    next.remove(router.getId());
                }
            }

            public void visit(IndexedTripleRouter router) {
                router.stop();
            }

            public void visit(RefreshableBDBGraphPatternRouter router) {
                router.stop();
            }

        });
    }

    public void unregisterSelectQuery(ContinuousSelect selectQuery) {
        unregisterQuery(selectQuery);
    }

    /**
     * register the construct-type query with the engine
     *
     * @param query
     * @return
     */
    public ContinuousConstruct registerConstructQuery(Query query) {
        OpRouter qR = generateRoutingPolicy(query);
        if (query.isConstructType()) {
            ///TODO
            ContinuousConstruct rootRouter = (ContinuousConstruct) addRouter(qR,
                    new ContinuousConstruct(context, query, qR));
            return rootRouter;
        }
        return null;
    }

    /**
     * @param filters
     * @param streamOps
     * @param others
     */
    private void project(List<ElementFilter> filters,
            List<Op> streamOps, List<Op> others, Query query) {

        HashSet<Var> upperVars = new HashSet<Var>();
        upperVars.addAll(query.getProjectVars());
        if (query.hasGroupBy()) {
            upperVars.addAll(query.getGroupBy().getVars());
            for (ExprAggregator agg : query.getAggregators()) {
                upperVars.addAll(agg.getVarsMentioned());
            }
        }

        if (query.hasHaving()) {
            if (query.hasHaving()) {
                for (Expr expr : query.getHavingExprs()) {
                    upperVars.addAll(expr.getVarsMentioned());
                }
            }
        }

        for (ElementFilter filter : filters) {
            upperVars.addAll(filter.getExpr().getVarsMentioned());
            //System.out.println(upperVars);
        }

        for (Op op : streamOps) {
            OpVars.allVars(op, upperVars);
            //System.out.println(upperVars);
        }

        for (int i = 0; i < others.size(); i++) {
            Op op = others.get(i);
            Set<Var> opVars = (Set<Var>) OpVars.allVars(op);
            ArrayList<Var> projectedVars = new ArrayList<Var>();
            for (Var var : opVars) {
                if (upperVars.contains(var)) {
                    projectedVars.add(var);
                }
            }
            if (projectedVars.size() < opVars.size()) {
                others.set(i, new OpDistinct(new OpProject(op, projectedVars)));
            } else {
                others.set(i, new OpDistinct(op));
            }
        }
    }

    private void addStreamOp(List<Op> streamOps, ElementStreamGraph el) {
        /*if(el.getWindow()==null){
         System.out.println("null");
         }
         else System.out.println(el.getWindow().getClass());*/
        if (el.getElement() instanceof ElementTriplesBlock) {
            addStreamOp(streamOps, (ElementTriplesBlock) el.getElement(),
                    el.getGraphNameNode(), el.getWindow());
        } else if (el.getElement() instanceof ElementGroup) {
            addStreamOp(streamOps, (ElementGroup) el.getElement(),
                    el.getGraphNameNode(), el.getWindow());
        } else {
            System.out.println("Stream pattern is not ElementTripleBlock" + el.getElement().getClass());
        }
    }

    private void addStreamOp(List<Op> streamOps,
            ElementGroup group, Node graphNode, Window window) {
        for (Element el : group.getElements()) {
            if (el instanceof ElementTriplesBlock) {
                addStreamOp(streamOps, (ElementTriplesBlock) el, graphNode, window);
            }
            if (el instanceof ElementPathBlock) {
                for (Iterator<TriplePath> paths = ((ElementPathBlock) el).patternElts(); paths.hasNext();) {
                    Triple t = paths.next().asTriple();
                    if (t != null) {
                        streamOps.add(new OpStream(graphNode, t, window));
                    } else {
                        System.out.println("Path is not supported");
                    }
                }
            } else {
                System.out.println("unrecognized block" + el.getClass());
            }
        }
    }

    private void addStreamOp(List<Op> streamOps,
            ElementTriplesBlock el, Node graphNode, Window window) {
        for (Triple t : el.getPattern().getList()) {
            streamOps.add(new OpStream(graphNode, t, window));
        }
    }

    class UpdateRefreshIntervalsTransformer extends TransformBase {

        private final Op op;
        private final Map<Op, Long> indirectRefreshIntervals = new HashMap<Op, Long>();

        public UpdateRefreshIntervalsTransformer(Op op) {
            this.op = op;
            findIndirectRefreshIntervals();
        }

        private void findIndirectRefreshIntervals() {
            // Find refreshable streams
            final List<OpRefreshableStreamGraph> refreshableStreams = new ArrayList<OpRefreshableStreamGraph>();
            final Map<Triple, Op> triplesMapping = new HashMap<Triple, Op>();
            OpWalker.walk(op, new OpVisitorBase() {
                @Override
                public void visit(final OpGraph opGraph) {
                    if (opGraph instanceof OpRefreshableStreamGraph) {
                        refreshableStreams.add((OpRefreshableStreamGraph) opGraph);
                    }
                    OpWalker.walk(opGraph, new OpVisitorBase() {
                        @Override
                        public void visit(OpTriple opTriple) {
                            triplesMapping.put(opTriple.getTriple(), opGraph);
                        }

                        @Override
                        public void visit(OpBGP opBGP) {
                            for (Triple triple : opBGP.getPattern().getList()) {
                                triplesMapping.put(triple, opGraph);
                            }
                        }
                    });
                }

                @Override
                public void visit(final OpService opService) {
                    OpWalker.walk(opService, new OpVisitorBase() {
                        @Override
                        public void visit(OpTriple opTriple) {
                            triplesMapping.put(opTriple.getTriple(), opService);
                        }

                        @Override
                        public void visit(OpBGP opBGP) {
                            for (Triple triple : opBGP.getPattern().getList()) {
                                triplesMapping.put(triple, opService);
                            }
                        }
                    });
                }
            });
            if (!refreshableStreams.isEmpty()) {
                for (OpRefreshableStreamGraph refreshableStream : refreshableStreams) {
                    Set<Op> dependentRefreshables = findDependendRefreshables(triplesMapping, refreshableStream.getNode());
                    for (Op op : dependentRefreshables) {
                        indirectRefreshIntervals.put(op,
                                indirectRefreshIntervals.containsKey(op)
                                ? Math.min(indirectRefreshIntervals.get(op), refreshableStream.getDuration().inNanoSec())
                                : refreshableStream.getDuration().inNanoSec());
                    }
                }
            }
        }

        private Set<Op> findDependendRefreshables(Map<Triple, Op> triplesMapping, Node nodeOfInterest) {
            Set<Op> foundDependencies = new HashSet<Op>();
            List<Node> foundNodes = new ArrayList<Node>();
            findDependentTriplesRecursive(triplesMapping, foundDependencies, foundNodes, nodeOfInterest);
            return foundDependencies;
        }

        private void findDependentTriplesRecursive(Map<Triple, Op> triplesMapping, Set<Op> foundDependencies, List<Node> foundNodes, Node nodeOfInterest) {
            Map<Node, List<Op>> newlyFoundNodes = new HashMap<Node, List<Op>>();
            Iterator<Map.Entry<Triple, Op>> iter = triplesMapping.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Triple, Op> entry = iter.next();
                Triple triple = entry.getKey();
                if (triple.subjectMatches(nodeOfInterest)
                        || triple.predicateMatches(nodeOfInterest)
                        || triple.objectMatches(nodeOfInterest)) {
                    if (triple.getSubject().isVariable() && !triple.getSubject().equals(nodeOfInterest) && !foundNodes.contains(triple.getSubject())) {
                        if (!newlyFoundNodes.containsKey(triple.getSubject())) {
                            newlyFoundNodes.put(triple.getSubject(), new ArrayList<Op>());
                        }
                        newlyFoundNodes.get(triple.getSubject()).add(entry.getValue());
                    }
                    if (triple.getPredicate().isVariable() && !triple.getPredicate().equals(nodeOfInterest) && !foundNodes.contains(triple.getPredicate())) {
                        if (!newlyFoundNodes.containsKey(triple.getPredicate())) {
                            newlyFoundNodes.put(triple.getPredicate(), new ArrayList<Op>());
                        }
                        newlyFoundNodes.get(triple.getPredicate()).add(entry.getValue());
                    }
                    if (triple.getObject().isVariable() && !triple.getObject().equals(nodeOfInterest) && !foundNodes.contains(triple.getObject())) {
                        if (!newlyFoundNodes.containsKey(triple.getObject())) {
                            newlyFoundNodes.put(triple.getObject(), new ArrayList<Op>());
                        }
                        newlyFoundNodes.get(triple.getObject()).add(entry.getValue());
                    } else {
                        foundDependencies.add(entry.getValue());
                    }
                    iter.remove();
                }

            }
            for (Map.Entry<Node, List<Op>> entry : newlyFoundNodes.entrySet()) {
                foundNodes.add(entry.getKey());
                foundDependencies.addAll(entry.getValue());
                findDependentTriplesRecursive(triplesMapping, foundDependencies, foundNodes, entry.getKey());
            }
        }

        @Override
        public Op transform(OpGraph opGraph, Op subOp) {
            Op result = opGraph;
            if (indirectRefreshIntervals.containsKey(opGraph)) {
                result = new OpRefreshableGraph(opGraph.getNode(), opGraph.getSubOp(),
                        new DurationSet(
                                (opGraph instanceof OpRefreshableGraph)
                                ? Math.min(((OpRefreshableGraph) opGraph).getDuration().inNanoSec(), indirectRefreshIntervals.get(opGraph))
                                : indirectRefreshIntervals.get(opGraph)));
            }
            return result;
        }

        @Override
        public Op transform(OpService opService, Op subOp) {
            Op result = opService;
            if (indirectRefreshIntervals.containsKey(opService)) {
                result = new OpRefreshableService(opService.getService(), opService.getSubOp(), opService.getSilent(),
                        new DurationSet(
                                (opService instanceof OpRefreshableService)
                                ? Math.min(((OpRefreshableService) opService).getDuration().inNanoSec(), indirectRefreshIntervals.get(opService))
                                : indirectRefreshIntervals.get(opService)));
            }
            return result;
        }
    }

}
