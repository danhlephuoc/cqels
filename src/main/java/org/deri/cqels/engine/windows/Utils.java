package org.deri.cqels.engine.windows;

import com.sleepycat.je.Database;

public class Utils {
	public static void deleteDB(Database db){
		db.getEnvironment().removeDatabase(null,db.getDatabaseName());
	}
}
