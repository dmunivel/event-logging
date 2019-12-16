package com.cs.event.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dmuni
 *
 */
public class AppProperties {

	private static final Logger logger = LoggerFactory.getLogger(AppProperties.class);

	private static Properties props;
	
	private static boolean propLoaded = false;

	static {
		if (props == null) {
			props = new Properties();
			try {
				InputStream is = AppProperties.class.getResourceAsStream("/app.properties");
				props.load(is);
				propLoaded = true;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * @return
	 */
	public static boolean isLoaded() {
		return propLoaded;
	}
}
