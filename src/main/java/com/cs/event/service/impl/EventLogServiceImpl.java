package com.cs.event.service.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.event.dao.EventLogDao;
import com.cs.event.dao.impl.EventLogDaoImpl;
import com.cs.event.model.EventLog;
import com.cs.event.service.EventLogService;
import com.cs.event.util.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service class for event log
 * @author dmuni
 *
 */
public class EventLogServiceImpl implements EventLogService {
	
	private static final Logger logger = LoggerFactory.getLogger(EventLogServiceImpl.class);

	public EventLogServiceImpl() {
	}
	
	/**
	 * Read the event from file and match the start/end event if the duration > 4 then insert into db. 
	 */
	@Override
	public void processFile(String path,Long batchId) {
		List<EventLog> insertLogs = new ArrayList<EventLog>();
		List<EventLog> searchLogs = new ArrayList<EventLog>();
		ObjectMapper objectMapper = new ObjectMapper();
		Stream<String> lines = null;
		try {
			ExecutorService executor = Executors.newFixedThreadPool(5);
			lines = Files.lines(Paths.get(path));
			Iterator<String> itr = lines.iterator();
			int batch_size =  Integer.parseInt(AppProperties.getProperty("app.batch.size"));
			logger.debug("The Batch Size::{}",batch_size);
			long noOflinesRead = 0;
			int index = 0;
			while (itr.hasNext()) {
				String line = itr.next();
				noOflinesRead++;
				EventLog eventLog = objectMapper.readValue(line, EventLog.class);
				eventLog.setBatchId(batchId);
				if ((index = searchLogs.indexOf(eventLog)) > -1 ) {
					EventLog log = checkEligibleToInsert(eventLog,searchLogs.get(index));
					if(log != null)
						insertLogs.add(log);
				} else {
					searchLogs.add(eventLog);
				}
				if (noOflinesRead % batch_size == 0) {
					insertLogs.addAll(findMatchLogs(path, searchLogs, noOflinesRead));
					storeEventLogs(insertLogs,executor);
					searchLogs.clear();
					insertLogs = new ArrayList<EventLog>();
				}
			}
			if (searchLogs.size() > 0)
				insertLogs.addAll(findMatchLogs(path, searchLogs, noOflinesRead));
			if (insertLogs.size() > 0)
				storeEventLogs(insertLogs,executor);
			lines.close();
			executor.shutdown();
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			if(lines != null)
				lines.close();
		}
	}
	
	/** Verify the eventlog duration is > 4
	 * @param el1
	 * @param el2
	 * @return EventLog
	 */
	private EventLog checkEligibleToInsert(EventLog el1, EventLog el2) {
		el1.setDuration((int) (el1.getTimestamp() - el2.getTimestamp()));
		if(el1.getDuration() < 0)
			el1.setDuration(el1.getDuration() * -1);
		if(el1.getDuration() > 4)
			el1.setEventAlert(true);
		if(el1.isEventAlert()) {
			return el1;
		}
		return null;
	}
	
	/** Store the event logs
	 * @param insertLogs
	 * @param executor
	 * @return 
	 */
	public CompletableFuture<Void> storeEventLogs(List<EventLog> insertLogs,Executor executor) {
		EventLogDao dao = new EventLogDaoImpl();
		return CompletableFuture.supplyAsync(() -> {
			try {
				return dao.saveEventLogs(insertLogs);
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}
		}, executor).exceptionally(exe -> {
			logger.error("got error on save the event log",exe);
			throw new RuntimeException(exe.getMessage());
		}).thenAccept(msg -> { logger.info("success message on save logs : {}",msg); });
	}
	
	
	/**
	 * Read the events from file and match the start/end event logs and store the event log into db insert list.
	 * @param path
	 * @param eventLogs
	 * @param noOflinesToSkip
	 * @return EventLog list for insert into db
	 * @throws Exception
	 */
	public List<EventLog> findMatchLogs(String path,List<EventLog> eventLogs,long noOflinesToSkip) throws Exception {
		List<EventLog> insertLogs = new ArrayList<EventLog>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			logger.debug("the number lines to skip {} and total records - {}",noOflinesToSkip,eventLogs.size());
			int index = -1;
			Stream<String> lines = Files.lines(Paths.get(path)).skip(noOflinesToSkip);
			Iterator<String> itr = lines.iterator();
			while (eventLogs.size() > 0 && itr.hasNext()) {
				String line = itr.next();
				index = findIndex(line,eventLogs);
				if (index > -1) {
					EventLog matchLog = objectMapper.readValue(line, EventLog.class);
					EventLog log = checkEligibleToInsert(eventLogs.get(index),matchLog);
					if(log != null)
						insertLogs.add(log);
					eventLogs.remove(index);
				}
			}
		} catch (Exception e) {
			logger.error("got error on search logs",e);
			throw e;
		}
		return insertLogs;
	}

	/**
	 * find the index of EventLog in the list using event id.
	 * @param line
	 * @param eventLogs
	 * @return the index of EventLog match with event id
	 */
	private int findIndex(String line,List<EventLog> eventLogs) {
		int index = -1;
		for (int i = 0; i < eventLogs.size(); i++) {
			EventLog log = eventLogs.get(i);
			if (line.contains(log.getId())) {
				index = i;
				break;
			}
		}
		return index;
	}


}
