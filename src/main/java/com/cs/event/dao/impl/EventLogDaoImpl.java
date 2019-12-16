package com.cs.event.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.event.dao.EventLogDao;
import com.cs.event.exception.DaoException;
import com.cs.event.model.EventLog;
import com.cs.event.util.AppProperties;
import com.cs.event.util.Constants;

public class EventLogDaoImpl implements EventLogDao {
	
	private static final Logger logger = LoggerFactory.getLogger(EventLogDaoImpl.class);

	@Override
	public String saveEventLogs(List<EventLog> eventLogs) throws DaoException {
		String returnMessage = "No of records inserted - %d from total records %d ";
		int noOfRowsInserted = 0;
		try {
			Connection db = DriverManager.getConnection(AppProperties.getProperty("db.conn.url"),
					AppProperties.getProperty("db.conn.user"), AppProperties.getProperty("db.conn.password"));
			for (EventLog log : eventLogs) {
				try {
					PreparedStatement ps = db.prepareCall(Constants.INSERT_EVENT_LOG_SQL);
					ps.setLong(1, log.getBatchId());
					ps.setString(2, log.getId());
					ps.setInt(3, log.getDuration());
					ps.setString(4, log.getType());
					ps.setString(5, log.getHost());
					ps.setString(6, log.isEventAlert() ? "Y" : "N");
					noOfRowsInserted += ps.executeUpdate();
					db.commit();
				} catch (Exception ex) {
					logger.error(String.format("Got error while insert data's ::{}", log.toString()), ex);
				}
			}
			returnMessage = String.format(returnMessage, noOfRowsInserted,eventLogs.size());
			db.close();
		} catch(Exception ex) {
			logger.error("Got unexpected error ", ex);
			throw new DaoException(ex.getMessage());
		}
		return returnMessage;
	}
}
