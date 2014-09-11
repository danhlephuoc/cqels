package org.deri.cqels.lang.cqels;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.syntax.Element;
import org.deri.cqels.engine.Window;

/**
 *
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class ElementRefreshableStreamGraph extends ElementStreamGraph {

    private final DurationSet duration;

    public ElementRefreshableStreamGraph(Node n, Window w, Element el, DurationSet duration) {
        super(n, w, el);
        this.duration = duration;
    }

    public DurationSet getDuration() {
        return duration;
    }
}
