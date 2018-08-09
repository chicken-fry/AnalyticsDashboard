package com.emirates.dash.model;

import java.util.List;

import org.apache.log4j.Level;

import com.emirates.dash.utilities.DashLoggerUtil;

public class RevenueBookingContainer {

	
	private static RevenueBookingContainer revenueBookingContainer;
	private List<RevenueBooking> revenueBookings;
	private static long lastLoaded;
	
	
	protected RevenueBookingContainer() {
	    // Exists only to defeat instantiation.
	}
	
	public synchronized static RevenueBookingContainer getInstance() {
		   if(revenueBookingContainer == null) {
			   revenueBookingContainer = new RevenueBookingContainer();
		   }
		   DashLoggerUtil.log(Level.INFO,"created singleton: " + revenueBookingContainer);
		   return revenueBookingContainer;
	}
	

	
	public static long getLastLoaded() {
		return lastLoaded;
	}

	public static void setLastLoaded(long lastLoaded) {
		RevenueBookingContainer.lastLoaded = lastLoaded;
	}

	public static void flushModel(){
		revenueBookingContainer = null;
//		delays = null;
		//lastLoaded flush??
	}

	public List<RevenueBooking> getRevenueBookings() {
		return revenueBookings;
	}

	public void setRevenueBookings(List<RevenueBooking> revenueBookings) {
		this.revenueBookings = revenueBookings;
	}
	
}
