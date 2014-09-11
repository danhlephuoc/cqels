package org.deri.cqels.engine;

import java.util.List;

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
 *  @email michael.jacoby@student.kit.edu
 */
public interface ConstructListener extends ContinuousListener {

    void setTemplate(Template t);

    void update(List<Triple> graph);

}
