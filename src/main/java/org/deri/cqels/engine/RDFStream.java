package org.deri.cqels.engine;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author	Danh Le Phuoc
 * @organization DERI Galway, NUIG, Ireland www.deri.ie
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email danh.lephuoc@deri.org
 * @email michael.jacoby@student.kit.edu
 */
public interface RDFStream {

    String getURI();

    void stop();

    void stream(Node s, Node p, Node o);

    void stream(String s, String p, String o);

    void stream(Triple t);

}
