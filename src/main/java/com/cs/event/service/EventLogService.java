package com.cs.event.service;

/**
 * Service class for Event log
 * @author dmuni
 *
 */
public interface EventLogService {

	/**
	 * Read the records from file and insert into db 
	 * @param path - file path
	 * @param batchId - batch id
	 */
	void processFile(String path,Long batchId);
	
}