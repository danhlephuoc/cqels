package org.deri.cqels.engine;

import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.openjena.atlas.io.IndentedWriter;

/**
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class QueryIteratorCache<T> {

    private final Map<T, QueryIteratorCopy> cache = new ConcurrentHashMap<T, QueryIteratorCopy>();

    public boolean containsKey(T key) {
        return cache.containsKey(key);
    }

    public void put(T key, QueryIterator value) {
        cache.put(key, new QueryIteratorCopy(value));
    }

    public QueryIterator get(T key) {
        if (containsKey(key)) {
            QueryIteratorCopy iterator = cache.get(key);
            return iterator.copy();
        }
        return null;
    }

    public QueryIterator remove(T key) {
        return cache.remove(key);
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public int size() {
        return cache.size();
    }

    @Override
    public int hashCode() {
        return cache.hashCode();
    }

    public void flush() {
        cache.clear();
    }

    @Override
    public boolean equals(Object o) {
        return cache.equals(o);
    }

    // copied from package com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCopy because class is not public
    class QueryIteratorCopy extends QueryIteratorBase {

        List<Binding> elements = new ArrayList<Binding>();
        QueryIterator iterator;

        QueryIterator original;        // Keep for debugging - This is closed as it is copied.

        public QueryIteratorCopy(QueryIterator qIter) {
            for (; qIter.hasNext();) {
                elements.add(qIter.nextBinding());
            }
            qIter.close();
            iterator = copy();
            original = qIter;
        }

        @Override
        protected Binding moveToNextBinding() {
            return iterator.nextBinding();
        }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {
            out.print("QueryIteratorCopy");
            out.incIndent();
            original.output(out, sCxt);
            out.decIndent();
        }

        public List<Binding> elements() {
            return Collections.unmodifiableList(elements);
        }

        public QueryIterator copy() {
            return new QueryIterPlainWrapper(elements.iterator());
        }

        @Override
        protected void closeIterator() {
            iterator.close();
        }

        @Override
        protected void requestCancel() {
            iterator.cancel();
        }

        @Override
        protected boolean hasNextBinding() {
            return iterator.hasNext();
        }
    }
}
