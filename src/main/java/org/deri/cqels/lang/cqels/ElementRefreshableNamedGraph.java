package org.deri.cqels.lang.cqels;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;

/**
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class ElementRefreshableNamedGraph extends ElementNamedGraph {

    private final DurationSet duration;

    public ElementRefreshableNamedGraph(Node n, Element el, DurationSet duration) {
        super(n, el);
        this.duration = duration;
    }

    public DurationSet getDuration() {
        return duration;
    }

}
