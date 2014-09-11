/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.deri.cqels.engine;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.OpVisitorByType;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpN;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import static com.hp.hpl.jena.sparql.engine.main.OpExecutor.createRootQueryIterator;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.tdb.solver.OpExecutorTDB;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.deri.cqels.data.Binding2Mapping;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.iterator.MappingIterCursorAll;
import org.deri.cqels.engine.iterator.MappingIterCursorByKey;
import org.deri.cqels.engine.iterator.MappingIterCursorByRangeKey;
import org.deri.cqels.engine.iterator.MappingIterator;
import org.deri.cqels.engine.iterator.NullMappingIter;
import org.deri.cqels.lang.cqels.OpRefreshable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class RefreshableBDBGraphPatternRouter extends OpRouterBase {

    ArrayList<String> idxDescs;
    HashMap<Var, Integer> var2Idx;
    protected ArrayList<Var> vars;
    ArrayList<ArrayList<Integer>> indexes;
    Database[] idxDbs; // 
    static final DatabaseEntry EMPTYDATA = new DatabaseEntry(new byte[0]);
    Database mainDB; //
    protected final DatasetGraph datasetGraph;
    protected final List<MappingIterator> openIterators = new ArrayList<MappingIterator>();

    public static Symbol SYMBOL_CACHE = Symbol.create("cache");
    public static Symbol SYMBOL_DO_NOT_CACHE = Symbol.create("not-cache");
    private ScheduledExecutorService executor;
    private final QueryIteratorCache<Op> resultCache = new QueryIteratorCache<Op>();
    private ExecutionContext executionContext;
    List<Binding> currentBindings = new ArrayList<Binding>();

    public RefreshableBDBGraphPatternRouter(ExecContext context, Op op) {
        super(context, op);
        this.datasetGraph = context.getDataset();
        init();
    }

    public RefreshableBDBGraphPatternRouter(ExecContext context, Op op, DatasetGraph ds) {
        super(context, op);
        this.datasetGraph = ds;
        init();
    }

    private void init() {
        initCacheStructure();
        executionContext = new ExecutionContext(context.getARQExCtx());
        executionContext.setExecutor(CachedOpExecutor.CachedOpExecFactory);
        executionContext.getContext().set(SYMBOL_CACHE, resultCache);
        executionContext.getContext().set(SYMBOL_DO_NOT_CACHE, new ArrayList<Op>());
        createRefreshables();
    }

    private void initCacheStructure() {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        mainDB = context.env().openDatabase(null, "pri_cache_" + getId(), dbConfig);
        vars = new ArrayList<Var>();
        var2Idx = new HashMap<Var, Integer>();
        Collection<Var> allVars = OpVars.allVars(op);
        for (Var var : allVars) {
            if (!var2Idx.containsKey(var)) {
                vars.add(var);
                var2Idx.put(var, vars.size() - 1);
            }
        }
        int lastIdx = vars.size() - 1;

        indexes = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> first = new ArrayList<Integer>();
        first.add(lastIdx);

        indexes.add(first);
        for (int i = 1; i < vars.size(); i++) {
            int size = indexes.size();
            for (int k = 0; k < size; k++) {
                ArrayList<Integer> next = new ArrayList<Integer>(indexes.get(k));
                next.add(lastIdx - i);
                indexes.add(next);
            }
        }
        idxDbs = new Database[indexes.size()];
        for (int i = 0; i < indexes.size() - 1; i++) {
            SecondaryConfig idxConfig = new SecondaryConfig();
            idxConfig.setAllowCreate(true);
            idxConfig.setSortedDuplicates(true);
            idxConfig.setTransactional(true);
            idxConfig.setKeyCreator(new KeyGenerator(indexes.get(i), vars.size()));

            idxDbs[i] = context.env().openSecondaryDatabase(null,
                    "idx_cache_" + getId() + "-" + i, mainDB, idxConfig);
        }
        idxDbs[indexes.size() - 1] = mainDB;
    }

    @Override
    public void _route(Mapping mapping) {
        mapping.from(this);
        // routing only allowed on one dataflow, so there does not always exist a next router
        if (context.policy().next(this, mapping) != null) {
            context.policy().next(this, mapping).route(mapping);
        }
    }

    public void stop() {
        executor.shutdownNow();
    }

    private synchronized void cancelOpenIterators() {
        Iterator<MappingIterator> iterator = openIterators.iterator();
        while (iterator.hasNext()) {
            iterator.next().cancel();
            iterator.remove();
        }
    }

    protected void clearCache(Transaction txn) {
        try {
            Cursor cursor = mainDB.openCursor(txn, null);
            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();
            while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                cursor.delete();
            }
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    protected synchronized void refresh() {
        Transaction txn = null;
        try {
            List<Binding> newBindings = new ArrayList<Binding>();
            QueryIterator itr = context.loadGraphPattern(op, datasetGraph);
            while (itr.hasNext()) {
                Binding binding = itr.next();
                newBindings.add(binding);
            }
            if (!checkBindingsAreEqual(newBindings)) {
                cancelOpenIterators();
                currentBindings = newBindings;
                txn = context.env().beginTransaction(null, TransactionConfig.DEFAULT);
                clearCache(txn);
                addBindings2Cache(txn, newBindings);
                txn.commit();
                for (Binding binding : newBindings) {
                    _route(new Binding2Mapping(context, binding));
                }
            }
        } catch (Exception ex) {
            if (txn != null) {
                txn.abort();
                txn = null;
            }
            ex.printStackTrace();
        }
    }

    private boolean checkBindingsAreEqual(List<Binding> newBindings) {
        if (newBindings == currentBindings) {
            return true;
        }
        if (newBindings.size() != currentBindings.size()) {
            return false;
        }
        for (Binding newBinding : newBindings) {
            if (!currentBindings.contains(newBinding)) {
                boolean found = false;
                for (Binding currentBinding : currentBindings) {
                    if (newBinding.equals(currentBinding)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addBindings2Cache(Transaction txn, List<Binding> bindings) {
        for (Binding binding : bindings) {
            addBinding2Cache(txn, binding);
        }
    }

    protected void addBinding2Cache(Transaction txn, Binding binding) {
        //System.out.println(binding);
        TupleOutput output = new TupleOutput();
        for (Var var : vars) {
            long value = context.engine().encode(binding.get(var));
            output.writeLong(value);
        }
        DatabaseEntry tmp = new DatabaseEntry(output.getBufferBytes());
        mainDB.put(txn, tmp, tmp);
    }

    @Override
    public MappingIterator getBuff() {
        return addToOpenIterators(new MappingIterCursorAll(context, mainDB, vars));
    }

    protected synchronized MappingIterator addToOpenIterators(MappingIterator iteratorToAdd) {
        openIterators.add(iteratorToAdd);
        return iteratorToAdd;
    }

    @Override
    public MappingIterator searchBuff4Match(Mapping mapping) {
        if (vars == null) {
            return NullMappingIter.instance();
        }

        //System.out.println("search " +mapping);
        ArrayList<Integer> idxMask = new ArrayList<Integer>();
        for (int i = 0; i < vars.size(); i++) {
            if (mapping.containsKey(vars.get(i))) {
                idxMask.add(i);
            }
        }
        //System.out.println(idxMask.size());
        int idx = -1, weight = 0;
        for (int i = 0; i < indexes.size(); i++) {
            int count = 0;
            int p = indexes.get(i).size() - 1;
            for (int l = 0; l < idxMask.size() && p >= 0; l++) {
                //	   System.out.println(idxMask.get(l)+ "--"+indexes.get(i).get(p) + " "+(idxMask.get(l).equals(indexes.get(i).get(p))));	
                if (!idxMask.get(l).equals(indexes.get(i).get(p--))) {
                    break;
                }
                count++;
            }
            //	System.out.println("count "+count);
            if (count > weight) {
                weight = count;
                idx = i;
            }
        }
        //	System.out.println("weight" +weight);
        if (weight > 0) {
            if (weight < indexes.get(idx).size()) {
                TupleOutput out = new TupleOutput();
                for (int i = 0; i < idxMask.size(); i++) {
                    out.writeLong(mapping.get(vars.get(idxMask.get(i))));
                }
                //add O mask for the lowest range key
                for (int i = 0; i < indexes.get(idx).size() - weight; i++) {
                    out.writeLong(0);
                }
                //System.out.println("return MappingIterCursorByRangeKey");
                return addToOpenIterators(new MappingIterCursorByRangeKey(context, idxDbs[idx],
                        new DatabaseEntry(out.getBufferBytes()),
                        mapping, vars, weight));
            } else {
                TupleOutput out = new TupleOutput();
                for (int i = 0; i < idxMask.size(); i++) {
                    out.writeLong(mapping.get(vars.get(idxMask.get(i))));
                }
                return addToOpenIterators(new MappingIterCursorByKey(context, idxDbs[idx],
                        new DatabaseEntry(out.getBufferBytes()),
                        mapping, vars));
            }
        }
        return NullMappingIter.instance();
    }

    public MappingIterator iterator() {
        return addToOpenIterators(new MappingIterCursorAll(context, mainDB, vars));
    }

    private void createRefreshables() {
        final List<OpRefreshable> refreshables = new ArrayList<OpRefreshable>();
        OpWalker.walk(op, new OpVisitorByType() {

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
                if (op instanceof OpRefreshable) {
                    refreshables.add((OpRefreshable) op);
                }
            }

        });
        executor = Executors.newScheduledThreadPool(refreshables.size());
        for (OpRefreshable refreshable : refreshables) {
            RefreshableOp refreshableOp = new RefreshableOp((Op) refreshable, executionContext);
            refreshableOp.addListener(new RefreshableOpListener() {

                public void bindingRefreshed(Op op, QueryIterator iterator) {
                    resultCache.put(op, iterator);
                    refresh();
                }
            });
            executor.scheduleAtFixedRate(refreshableOp, refreshable.getDuration().inNanoSec(), refreshable.getDuration().inNanoSec(), TimeUnit.NANOSECONDS);
        }
        if (refreshables.isEmpty()) {
            refresh();
        }
    }

    public void visit(RouterVisitor rv) {
        rv.visit(this);
    }

    interface RefreshableOpListener {

        public void bindingRefreshed(Op op, QueryIterator iterator);
    }

    class RefreshableOp implements Runnable {

        private final List<RefreshableOpListener> listeners = new ArrayList<RefreshableOpListener>();
        private final ExecutionContext executionContext;
        private final Op op;

        public RefreshableOp(Op op, ExecutionContext executionContext) {
            this.op = op;
            this.executionContext = new ExecutionContext(executionContext.getContext().copy(), executionContext.getActiveGraph(), executionContext.getDataset(), executionContext.getExecutor());
            this.executionContext.getContext().set(SYMBOL_DO_NOT_CACHE, Arrays.asList(op));
        }

        public void addListener(RefreshableOpListener toAdd) {
            listeners.add(toAdd);
        }

        public void removeListener(RefreshableOpListener toAdd) {
            listeners.add(toAdd);
        }

        private void fireBindingRefreshed(QueryIterator iterator) {
            for (RefreshableOpListener listener : listeners) {
                listener.bindingRefreshed(op, iterator);
            }
        }

        public void run() {
            execute();
        }

        private void execute() {
            try {
                QueryIterator result = QC.execute(op, CachedOpExecutor.createRootQueryIterator(executionContext), executionContext);
                fireBindingRefreshed(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    @Override
    protected void finalize() {
        try {
            if (executor != null) {
                executor.shutdownNow();
            }
        } finally {
            try {
                super.finalize();
            } catch (Throwable ex) {
                java.util.logging.Logger.getLogger(RefreshableBDBGraphPatternRouter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

class KeyGenerator implements SecondaryKeyCreator {

    ArrayList<Integer> idxList;
    int size;

    public KeyGenerator(ArrayList<Integer> idxList, int size) {
        this.idxList = idxList;
        this.size = size;
    }

    /**
     * @param secondary
     * @param key
     * @param data
     * @param result
     */
    public boolean createSecondaryKey(SecondaryDatabase secondary,
            DatabaseEntry key, DatabaseEntry data, DatabaseEntry result) {
        TupleInput dataInput = new TupleInput(data.getData());
        TupleOutput indexKeyOutput = new TupleOutput();
        int p = idxList.size() - 1;
        for (int l = 0; l < size && p >= 0; l++) {
            long tmp = dataInput.readLong();
            if (l == idxList.get(p)) {
                indexKeyOutput.writeLong(tmp);
                p--;
            }
        }
        result.setData(indexKeyOutput.getBufferBytes());
        return true;
    }
}

class CachedOpExecutor extends OpExecutorTDB {

    private static final Logger log = LoggerFactory.getLogger(CachedOpExecutor.class);

    protected static final OpExecutorFactory CachedOpExecFactory = new OpExecutorFactory() {
        @Override
        public OpExecutor create(ExecutionContext execCxt) {
            return new CachedOpExecutor(execCxt);
        }
    };

    private static OpExecutor createOpExecutor(ExecutionContext execCxt) {
        OpExecutorFactory factory = execCxt.getExecutor();
        if (factory == null) {
            factory = CachedOpExecFactory;
        }
        if (factory == null) {
            return new CachedOpExecutor(execCxt);
        }
        return factory.create(execCxt);
    }

    static QueryIterator execute(Op op, ExecutionContext execCxt) {
        return execute(op, createRootQueryIterator(execCxt), execCxt);
    }

    static QueryIterator execute(Op op, QueryIterator qIter, ExecutionContext execCxt) {
        OpExecutor exec = createOpExecutor(execCxt);
        QueryIterator q = exec.executeOp(op, qIter);
        return q;
    }

    private final QueryIteratorCache<Op> cachedOps;
    private final List<Op> notCachedOps;

    protected CachedOpExecutor(ExecutionContext execCxt) {
        super(execCxt);
        if (execCxt.getContext().isDefined(RefreshableBDBGraphPatternRouter.SYMBOL_CACHE)) {
            cachedOps = (QueryIteratorCache<Op>) execCxt.getContext().get(RefreshableBDBGraphPatternRouter.SYMBOL_CACHE);
        } else {
            cachedOps = new QueryIteratorCache<Op>();
        }
        if (execCxt.getContext().isDefined(RefreshableBDBGraphPatternRouter.SYMBOL_DO_NOT_CACHE)) {
            notCachedOps = (List<Op>) execCxt.getContext().get(RefreshableBDBGraphPatternRouter.SYMBOL_DO_NOT_CACHE);
        } else {
            notCachedOps = new ArrayList<Op>();
        }
    }

    @Override
    public QueryIterator executeOp(Op op, QueryIterator input) {
        if (!notCachedOps.contains(op) && cachedOps.containsKey(op)) {
            return cachedOps.get(op);
        }
        return super.executeOp(op, input);
    }

}
