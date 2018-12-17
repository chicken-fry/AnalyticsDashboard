package com.emirates.dash.utilities;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class DashLoggerUtil {


  
    /**
     * The log object will be used to invoke the logging methods that logs to
     * console.
     */
    public static   org.apache.log4j.Logger log =  null;

    
    private static final Logger dashLogger = Logger.getLogger("dashLogger");
    

	public static Logger getLogger(Class class1) {
		return Logger.getLogger(class1);
	}
	
	public static Logger getLogger(String loggerName) {
		return Logger.getLogger(loggerName);
	}
	
	
    public static void initialize(){
   	 /** To initialize the property-configurator */
   	final String PATH_CONFIG_PROPERTIES_FILE = "../ibedashboard_node1/config/dashProps/dash_log4j.properties";
//    final String PATH_CONFIG_PROPERTIES_FILE = "../standalone/config/dashProps/dash_log4j.properties";
   	
   	PropertyConfigurator.configure(PATH_CONFIG_PROPERTIES_FILE);
   	log = org.apache.log4j.Logger.getLogger(DashLoggerUtil.class);

   }
    /**
     * The method will tell whether the particular log4j level is enabled.
     * 
     * @param level
     * @return
     */
    public static boolean isLevelEnabled(Level level) {
	if (Level.TRACE == level) {
	    return log.isTraceEnabled();
	}
	if (Level.DEBUG == level) {
	    return log.isDebugEnabled();
	}
	if (Level.INFO == level) {
	    return log.isInfoEnabled();
	} else {
	    throw new IllegalArgumentException("The level " + level
		    + " cannot be evaluvated");
	}
    }

    /**
     * Log a message
     * 
     * @param level
     * @param message
     */
    public static void log(Level level, String message) {
	if (Level.TRACE == level && log.isTraceEnabled()) {
	    log.trace(message);
	}
	if (Level.DEBUG == level && log.isDebugEnabled()) {
	    log.debug(message);
	}
	if (Level.INFO == level && log.isInfoEnabled()) {
	    log.info(message);
	}
	if (Level.WARN == level) {
	    log.warn(message);
	}
	if (Level.ERROR == level) {
	    log.error(message);
	}
	if (Level.FATAL == level) {
	    log.fatal(message);
	}
    }

    /**
     * Log a message
     * 
     * @param level
     * @param t
     */
    public static void log(Level level, Throwable t) {
	if (Level.TRACE == level && log.isTraceEnabled()) {
	    log.trace(t.getMessage(), t);
	}
	if (Level.DEBUG == level && log.isDebugEnabled()) {
	    log.debug(t.getMessage(), t);
	}
	if (Level.INFO == level && log.isInfoEnabled()) {
	    log.info(t.getMessage(), t);
	}
	if (Level.WARN == level) {
	    log.warn(t.getMessage(), t);
	}
	if (Level.ERROR == level) {
	    log.error(t.getMessage(), t);
	}
	if (Level.FATAL == level) {
	    log.fatal(t.getMessage(), t);
	}
    }
    
    
    /**
     * Log a message
     * 
     * @param level
     * @param t
     */
    public static void log(Level level, String message, Throwable t) {
	if (Level.TRACE == level && log.isTraceEnabled()) {
	    log.trace(message+" - "+t.getMessage(), t);
	}
	if (Level.DEBUG == level && log.isDebugEnabled()) {
	    log.debug(message+" - "+t.getMessage(), t);
	}
	if (Level.INFO == level && log.isInfoEnabled()) {
	    log.info(message+" - "+t.getMessage(), t);
	}
	if (Level.WARN == level) {
	    log.warn(message+" - "+t.getMessage(), t);
	}
	if (Level.ERROR == level) {
	    log.error(message+" - "+t.getMessage(), t);
	}
	if (Level.FATAL == level) {
	    log.fatal(message+" - "+t.getMessage(), t);
	}
    }
}
