package com.emirates.dash.utilities;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.emirates.dash.engine.DataFlowOrchestrator;


public class OverlayDataRefreshScheduler extends TimerTask{


	
		private static boolean run = true;
		private static Timer timer = null;
		
		@Override
		public void run() {
		
			if(run){
				DataFlowOrchestrator.getInstance().initiateDataRefreshForOverlay();	
			}else if(timer!=null){
				timer.cancel();
				timer.purge();			
			}
			
		}
		
		
		public static void startTimer(){
			
			TimerTask timerTask = new OverlayDataRefreshScheduler();
			//timer = new Timer(true);//run as a daemon
			timer = new Timer();
			timer.scheduleAtFixedRate(timerTask, getDateOfFirstExecution(), 60*1000*60*24);
//			timer.scheduleAtFixedRate(timerTask, getDateOfFirstExecutionTest(), 60*1000*2);
			
		}
		
		public static void stopTimer(){
			run = false;
		}
		
		//starts the next day at 00:00 hrs
		private static Date getDateOfFirstExecution(){
			
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND, 0);
			
			Date dateOfFirstExec = cal.getTime();
			
			return dateOfFirstExec;
		}
		
		//starts the next day at 00:00 hrs
		private static Date getDateOfFirstExecutionTest(){
					
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(Calendar.MINUTE, 5);
			Date dateOfFirstExec = cal.getTime();
					
			return dateOfFirstExec;
		}
		


	
}
