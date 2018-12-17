package com.emirates.dash.vo;

import java.util.Hashtable;
import org.apache.log4j.Level;
import com.emirates.dash.utilities.DashLoggerUtil;

public class ModeTimeStampVO {

	private static ModeTimeStampVO modeTimeStampVO;
	private static Integer lastLoadedId;
	
	
	public static Integer getLastLoadedId() {
		return lastLoadedId;
	}

	public static void setLastLoadedId(Integer lastLoadedId) {
		ModeTimeStampVO.lastLoadedId = lastLoadedId;
	}

	protected ModeTimeStampVO() {
	    // Exists only to defeat instantiation.
	}
	
	public synchronized static ModeTimeStampVO getInstance() {
		   if(modeTimeStampVO == null) {
			   modeTimeStampVO = new ModeTimeStampVO();
		   }
		   if(modeTimeStampVO.getModeTime()==null){
			   modeTimeStampVO.setModeTime(new Hashtable<String,Long>());
		   }
		   //DashLoggerUtil.log(Level.DEBUG,"created/retrieved singleton: " + modeTimeStampVO);
		   return modeTimeStampVO;
	}
	
	private Hashtable<String,Long> modeTime;

	public Hashtable<String, Long> getModeTime() {
		return modeTime;
	}

	public void setModeTime(Hashtable<String, Long> modeTime) {
		this.modeTime = modeTime;
	}
	
	
}
