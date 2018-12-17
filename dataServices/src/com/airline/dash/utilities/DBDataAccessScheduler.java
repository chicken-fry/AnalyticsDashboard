package com.emirates.dash.utilities;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;

import com.emirates.dash.engine.DataFlowOrchestrator;
import com.emirates.dash.vo.CacheVOContainer;
import com.emirates.dash.vo.ModeTimeStampVO;

public class DBDataAccessScheduler extends TimerTask {

	
	private static boolean run = true;
	private static boolean terminate = false;
	private static Timer timer = null;
	
	@Override
	public void run() {
		if(run){
			DashLoggerUtil.log(Level.INFO, "minutely timer called");
			CacheVOContainer cacheVOContainer = null;
			for(Mode thisMode : Mode.values()){	
			   //all overlays will have same timestamp for that mode
			
			   try {
			   if(thisMode.equals(Mode.PAIDSEAT)) {
				   cacheVOContainer = (CacheVOContainer)DashCacheUtil.getDataFromCache(thisMode+"-"+Overlay.TODAY);
	               if(cacheVOContainer!=null && null!=cacheVOContainer.getLastLoadedTime()) {
	            	   
	            	  DashLoggerUtil.log(Level.INFO, "minutely timer called : cacheVOContainer.getLastLoadedTime() is empty");
	            	  if(ModeTimeStampVO.getInstance().getModeTime().containsKey(thisMode+"-"+Overlay.TODAY)) {
	            		  ModeTimeStampVO.getInstance().getModeTime().replace(thisMode+"-"+Overlay.TODAY, cacheVOContainer.getLastLoadedTime());
	            	  }else {
	            		  ModeTimeStampVO.getInstance().getModeTime().put(thisMode+"-"+Overlay.TODAY, cacheVOContainer.getLastLoadedTime());
	            	  }
	               }else {
	             	  if(ModeTimeStampVO.getInstance().getModeTime().containsKey(thisMode+"-"+Overlay.TODAY)) {
	             		  ModeTimeStampVO.getInstance().getModeTime().replace(thisMode+"-"+Overlay.TODAY, System.currentTimeMillis());
	            	  }else {
	            		  ModeTimeStampVO.getInstance().getModeTime().put(thisMode+"-"+Overlay.TODAY, System.currentTimeMillis());
	            	  }
	            	   	  
	               }
//	               if(ModeTimeStampVO.getLastLoadedId()==null) {
//	            	   ModeTimeStampVO.setLastLoadedId(0);
//	               }
	               
			   }
			   }catch (Exception e) {
				   DashLoggerUtil.log(Level.ERROR, "minutely timer error" + e.getMessage());
				   e.printStackTrace();
				   
				   if(ModeTimeStampVO.getInstance().getModeTime().containsKey(thisMode+"-"+Overlay.TODAY)) {
	             		  ModeTimeStampVO.getInstance().getModeTime().replace(thisMode+"-"+Overlay.TODAY, System.currentTimeMillis());
				   }else {
	            		  ModeTimeStampVO.getInstance().getModeTime().put(thisMode+"-"+Overlay.TODAY, System.currentTimeMillis());
				   }
				   
				   
			   }
            }
			DataFlowOrchestrator.getInstance().initiateReadCycle(ModeTimeStampVO.getInstance());	
		}else if(timer!=null && terminate){
			timer.cancel();
			timer.purge();			
		}
		
	}
	
	//Should be called once only at deployment
	public static void startTimer(){
		
		TimerTask timerTask = new DBDataAccessScheduler();
		timer = new Timer();
		run = true;
		//timer = new Timer(true);//run as a daemon
		timer.scheduleAtFixedRate(timerTask, getDateOfFirstExecution(), 60*1000);
		
	}
	
	public static void pauseTimer(){
		run = false;
	}
	
	public static void stopTimer(){
		run = false;
		terminate = true;
	}
	
	public static void unpauseTimer(){
		run = true;
	}
	
	//starts approximately a minute after application lifecycle start
	private static Date getDateOfFirstExecution(){
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.SECOND, 1);
		Date dateOfFirstExec = cal.getTime();
		
		return dateOfFirstExec;
	}
	
}
