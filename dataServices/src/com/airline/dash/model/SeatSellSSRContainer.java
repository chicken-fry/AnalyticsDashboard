package com.emirates.dash.model;

import java.util.List;

import org.apache.log4j.Level;

import com.emirates.dash.utilities.DashLoggerUtil;

public class SeatSellSSRContainer {

	private static SeatSellSSRContainer seatSellSSRContainer;
	private List<SeatSellSSR> seatSellSSRs;
	private static Long lastLoaded;
	
	
	protected SeatSellSSRContainer() {
	    // Exists only to defeat instantiation.
	}
	
	public synchronized static SeatSellSSRContainer getInstance() {
		   if(seatSellSSRContainer == null) {
			   seatSellSSRContainer = new SeatSellSSRContainer();
		   }
		   DashLoggerUtil.log(Level.DEBUG,"created/retrieved singleton: seatSellSSRContainer");
		   return seatSellSSRContainer;
	}
	

	public List<SeatSellSSR> getSeatSellSSRs() {
		return seatSellSSRs;
	}

	public void setSeatSellSSRs(List<SeatSellSSR> seatSellSSRs) {
		this.seatSellSSRs = seatSellSSRs;
	}


	public static Long getLastLoaded() {
		return lastLoaded;
	}

	public static void setLastLoaded(Long lastLoaded) {
		SeatSellSSRContainer.lastLoaded = lastLoaded;
	}

	public static void flushModel(){
		seatSellSSRContainer = null;
//		delays = null;
		//lastLoaded flush??
	}
}
