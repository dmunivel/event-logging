package com.cs.event.dao;

import java.util.List;

import com.cs.event.exception.DaoException;
import com.cs.event.model.EventLog;

public interface EventLogDao {

	String saveEventLogs(List<EventLog> eventLogs) throws DaoException;

}
