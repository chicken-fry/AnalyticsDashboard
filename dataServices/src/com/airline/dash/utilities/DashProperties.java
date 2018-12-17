package com.emirates.dash.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Level;


public class DashProperties {
	
	

	public static  String DASH_SERVER_PROPERTIES_LOCATION = "../ibedashboard_node1/config/dashProps/dash.properties";
//	public static  String DASH_SERVER_PROPERTIES_LOCATION = "../standalone/config/dashProps/dash.properties";
	public static final Properties appProperties = new Properties();

	public static void initialize(){
 	
	 	try {
	 	    // Server Specific Properties
			appProperties.load(new FileInputStream(DASH_SERVER_PROPERTIES_LOCATION));
		    // putting IP Address in Properties Object
		    InetAddress addr = InetAddress.getLocalHost();
		    appProperties.put("SERVER_IP_ADDRESS",
			    addr.getHostAddress());
	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 	
		for (Object key : appProperties.keySet()) {
		    DashLoggerUtil.log(Level.DEBUG, key + " : "
			    + appProperties.get(key));
		}
	

	}
	
	
	
    /*
     * return property value
     */
    public static String getProperty(String key) {

	if (appProperties.getProperty(key) != null) {
	    return appProperties.getProperty(key).trim();
	}
	return null;
    }

    /*
     * return default value if property is not exist in the hashmap
     */
    public static String getProperty(String key, String defaultValue) {
	return getProperty(key) == null ? defaultValue : getProperty(key);
    }

    public static int getPropertyAsInt(String key, String defaultValue) {
	String value = getProperty(key) == null ? defaultValue
		: getProperty(key);
	return Integer.parseInt(value);
    }

    public static long getPropertyAsLong(String key, String defaultValue) {
	String value = getProperty(key) == null ? defaultValue
		: getProperty(key);
	return Long.parseLong(value);
    }

    public static byte getPropertyAsByte(String key, String defaultValue) {
	String value = getProperty(key) == null ? defaultValue
		: getProperty(key);
	return Byte.parseByte(value);
    }

    public static boolean getPropertyAsBoolean(String key, String defaultValue) {
	String value = getProperty(key) == null ? defaultValue
		: getProperty(key);
	return Boolean.parseBoolean(value);
    }
}



