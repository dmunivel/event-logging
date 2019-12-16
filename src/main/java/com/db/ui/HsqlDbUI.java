package com.db.ui;

import org.hsqldb.util.DatabaseManagerSwing;

/**
 * class for view the HSQL in UI
 * @author dmuni
 *
 */
public class HsqlDbUI {
	public static void main(String[] args) {
		System.out.println("Launching manager");
		DatabaseManagerSwing.main(new String[] { 
				"--url", "jdbc:hsqldb:file:db-data/mydatabase","--user", "sa", "--password", "", "--noexit"});
	}
}
