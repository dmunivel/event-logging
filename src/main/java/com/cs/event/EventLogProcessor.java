package com.cs.event;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.event.service.EventLogService;
import com.cs.event.service.impl.EventLogServiceImpl;
import com.cs.event.util.AppProperties;
import com.cs.event.util.Constants;

/**
 * @author dmuni
 *
 */
public class EventLogProcessor {

	private static final Logger logger = LoggerFactory.getLogger(EventLogProcessor.class);

	public static void main(String[] args) throws Exception {
		try {
			if (args.length == 0) {
				logger.error("File path is Mandatory");
				System.exit(1);
			}
			String path = args[0];
			if (!AppProperties.isLoaded()) {
				logger.error("app.properties is not loaded");
				System.exit(1);
			}
			try {
				Class.forName("org.hsqldb.jdbc.JDBCDriver");
			} catch (ClassNotFoundException e) {
				logger.error("Error on loading JDBC Driver");
				System.exit(1);
			}
			Long batchId = initdb();
			logger.debug("reading file form path - {}", path);
			logger.info("The batch id : {}", batchId);
			EventLogService service = new EventLogServiceImpl();
			service.processFile(path, batchId);
			logger.info("File process end");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

		}
	}

	public static Long initdb() throws Exception {
		Long batchId = 0l;
		try {
			String createEvent = readToString("sql/create_event.sql");
			String truncateEvent = readToString("sql/truncate_event.sql");
			Connection dbconn = DriverManager.getConnection(AppProperties.getProperty("db.conn.url"),
					AppProperties.getProperty("db.conn.user"), AppProperties.getProperty("db.conn.password"));
			Statement stat = dbconn.createStatement();
			stat.executeUpdate(createEvent);
			stat.executeUpdate(truncateEvent);
			dbconn.commit();
			ResultSet rs = stat.executeQuery(Constants.FETCH_BATCH_ID_SQL);
			while(rs.next()) {
				batchId = rs.getLong("batch_id");
			}
			dbconn.close();
			logger.info("DB init completed");
		} catch (Exception ex) {
			logger.error("error on init db", ex);
		}
		return ++batchId;
	}

	public static String readToString(String fname) throws Exception {
		File file = new File(fname);
		return org.apache.commons.io.FileUtils.readFileToString(file, "utf-8");
	}
}
