package com.cs.event.util;

/**
 * @author dmuni
 *
 */
public class Constants {
	
	public static String FETCH_BATCH_ID_SQL = "select MAX(batch_id) as batch_id from public.event_logs";
	
	public static String INSERT_EVENT_LOG_SQL = "insert into PUBLIC.EVENT_LOGS(batch_id,event_id,event_duration,log_type,host,event_alert)"
			+ " values(?,?,?,?,?,?)";
}
