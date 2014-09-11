package org.deri.cqels.engine;

import org.deri.cqels.data.Mapping;

import com.hp.hpl.jena.query.Query;

/**
 * This interface contains set of methods which are behaviors of routing policy
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
public interface RoutingPolicy {

    public OpRouter next(OpRouter curRouter, Mapping mapping);

    public ContinuousSelect registerSelectQuery(Query query);

    public ContinuousConstruct registerConstructQuery(Query query);

    public void unregisterSelectQuery(ContinuousSelect query);

    public void unregisterConstructQuery(ContinuousConstruct query);
}
