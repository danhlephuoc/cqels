package org.deri.cqels.lang.cqels;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementService;

/**
 *
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email michael.jacoby@student.kit.edu
 */
public class ElementRefreshableService extends ElementService {

    private final DurationSet duration;

    public ElementRefreshableService(String serviceURI, Element el, boolean silent, DurationSet duration) {
        super(serviceURI, el, silent);
        this.duration = duration;
    }

    public ElementRefreshableService(Node n, Element el, boolean silent, DurationSet duration) {
        super(n, el, silent);
        this.duration = duration;
    }

    public DurationSet getDuration() {
        return duration;
    }
}
