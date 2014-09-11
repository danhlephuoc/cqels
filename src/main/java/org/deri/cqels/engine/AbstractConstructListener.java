package org.deri.cqels.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deri.cqels.data.Mapping;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * This class processes the mapping result for the CONSTRUCT-type query form
 *
 * @author Danh Le Phuoc
 * @author Chan Le Van
 * @organization DERI Galway, NUIG, Ireland www.deri.ie
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email danh.lephuoc@deri.org
 * @email chan.levan@deri.org
 * @email michael.jacoby@student.kit.edu
 */
public abstract class AbstractConstructListener implements ConstructListener {

    private String uri;
    private Template template;
    private ExecContext context;
    public static int count = 0;

    public AbstractConstructListener(ExecContext context, String streamURI) {
        this.uri = streamURI;
        this.context = context;
    }

    public AbstractConstructListener(ExecContext context) {
        this.context = context;
    }

    @Override
    public void setTemplate(Template t) {
        template = t;
    }

    @Override
    public void update(Mapping mapping) {
        List<Triple> triples = getTemplate().getTriples();
        Iterator<Triple> ti = triples.iterator();
        ArrayList<Triple> graph = new ArrayList<Triple>();
        while (ti.hasNext()) {
            Triple triple = ti.next();
            Node s, p, o;

            if (triple.getSubject().isVariable()) {
                s = getContext().engine.decode(mapping.get(triple.getSubject()));
            } else {
                s = triple.getSubject();
            }

            if (triple.getPredicate().isVariable()) {
                p = getContext().engine.decode(mapping.get(triple.getPredicate()));
            } else {
                p = triple.getPredicate();
            }

            if (triple.getObject().isVariable()) {
                o = getContext().engine.decode(mapping.get(triple.getObject()));
            } else {
                o = triple.getObject();
            }
            graph.add(new Triple(s, p, o));
        }
        update(graph);
        count++;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @param context the context to set
     */
    public void setContext(ExecContext context) {
        this.context = context;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * @return the context
     */
    public ExecContext getContext() {
        return context;
    }

}
