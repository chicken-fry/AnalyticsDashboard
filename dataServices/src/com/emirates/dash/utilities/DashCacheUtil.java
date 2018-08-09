package com.emirates.dash.utilities;

import java.util.Hashtable;
import org.apache.log4j.Level;
import com.emirates.dash.vo.CacheVOContainer;


public class DashCacheUtil {

	    //the key string will be 'mode-overlay'
		private static Hashtable<String, Object> dashCacheHashMap = null;

		static {
			if (dashCacheHashMap == null) {
				dashCacheHashMap = new Hashtable<String, Object>();
			}
		}

		/**
		 * To set the data in the cache
		 * 
		 * @param args
		 *            key : the unique key-identification for the Object to be
		 *            cached value : The object to be cached
		 */
		public  static synchronized void setDataInCache(String key, Object value, Long lastLoaded) {

			if (key != null && !key.trim().equals("") && value != null) {
					if(value instanceof CacheVOContainer){
						((CacheVOContainer) value).setLastLoadedTime(lastLoaded);
					}
					dashCacheHashMap.put(key, value);
			}
			else {
				throw new IllegalArgumentException(
						"Invalid arguments passed. Please provide a valid key and value : Key="
								+ key + " / Value=" + value);
			}
		}

		/**
		 * To retrieve data from the cache
		 * 
		 * @param args
		 *            key : the unique key-identification to get the Object from the
		 *            cache
		 */
		public static Object getDataFromCache(String key) {
			if (key != null && !key.trim().equals("")) {
				if (dashCacheHashMap.containsKey(key)) {
					Object value = dashCacheHashMap.get(key);
					
					if(value instanceof CacheVOContainer){
						//**not sure if below is required to be updated here
						//((CacheVOContainer) value).setLastLoadedTime(System.currentTimeMillis());
					}
					return value;
				} else {				
					DashLoggerUtil.log(Level.ERROR,
							"The Key is not found in the cache");
					return null;
				}
			} else {
				throw new IllegalArgumentException(
						"Invalid arguments passed. Please provide a valid key : Key="
								+ key);
			}
		}

		public static synchronized boolean cleanCache(String key) {
			//if (key != null && !key.trim().equals("")) {
				//if (ibeCacheHashMap.containsKey(key)) {
				if(dashCacheHashMap.remove(key) != null){
					//if (ibeCacheHashMap.remove(key) != null) {
					DashLoggerUtil.log(Level.DEBUG,"##################The Key is deleted-------------------");					
						return true;
					//}
				} else if("CLEAR_CACHE".equals(key)) {
					dashCacheHashMap.clear();
					DashLoggerUtil.log(Level.DEBUG,"##################Entire Cache is deleted-------------------");				
					return true;
				}
			//}
			return false;
		}
		

		public static Hashtable<String, Object> getDashCacheHashMap() {
			return dashCacheHashMap;
		}
		
	
}
