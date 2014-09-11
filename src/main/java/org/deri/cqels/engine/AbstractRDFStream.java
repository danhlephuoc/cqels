package org.deri.cqels.engine;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Danh Le Phuoc
 * @organization DERI Galway, NUIG, Ireland www.deri.ie
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email danh.lephuoc@deri.org
 * @email michael.jacoby@student.kit.edu
 */
public abstract class AbstractRDFStream implements RDFStream {

    protected Node streamURI;
    protected ExecContext context;

    public AbstractRDFStream(ExecContext context, String uri) {
        streamURI = Node.createURI(uri);
        this.context = context;
    }

    @Override
    public void stream(Node s, Node p, Node o) {
        context.engine().send(streamURI, s, p, o);
    }

    @Override
    public void stream(String s, String p, String o) {
        context.engine().send(streamURI, n(s), n(p), n(o));
    }

    @Override
    public void stream(Triple t) {
        stream(t.getSubject(), t.getPredicate(), t.getObject());
    }

    protected static Node n(String st) {
        return Node.createURI(st);
    }

    @Override
    public String getURI() {
        return streamURI.getURI();
    }
}
