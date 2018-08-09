package com.emirates.dash.model;

import java.util.List;

import org.apache.log4j.Level;

import com.emirates.dash.utilities.DashLoggerUtil;

public class PSPBookingContainer {

	private static PSPBookingContainer pspBookingContainer;

	//CHANGED
//	private List<PSPBooking> pspBookings;
//	private List<SeatSellSSR> seatSellSSRs;
	private List<PSPBooking> pspBookingsForTheYear;
	private List<SSRAndPaidSeatData> pspBookingsNew;
    private List<SSRAndPaidSeatData> pspBookingsForTheYearNew;
    
    
	
	public List<SSRAndPaidSeatData> getPspBookingsNew() {
		return pspBookingsNew;
	}

	public void setPspBookingsNew(List<SSRAndPaidSeatData> pspBookingsNew) {
		this.pspBookingsNew = pspBookingsNew;
	}

	public List<SSRAndPaidSeatData> getPspBookingsForTheYearNew() {
		return pspBookingsForTheYearNew;
	}

	public void setPspBookingsForTheYearNew(List<SSRAndPaidSeatData> pspBookingsForTheYearNew) {
		this.pspBookingsForTheYearNew = pspBookingsForTheYearNew;
	}

	private static Long lastLoaded;
	private static Integer lastLoadedId;
	
	
	
	
	public static PSPBookingContainer getPspBookingContainer() {
		return pspBookingContainer;
	}

	public static void setPspBookingContainer(PSPBookingContainer pspBookingContainer) {
		PSPBookingContainer.pspBookingContainer = pspBookingContainer;
	}

	public static Integer getLastLoadedId() {
		return lastLoadedId;
	}

	public static void setLastLoadedId(Integer lastLoadedId) {
		PSPBookingContainer.lastLoadedId = lastLoadedId;
	}

	protected PSPBookingContainer() {
	    // Exists only to defeat instantiation.
	}
	
	public synchronized static PSPBookingContainer getInstance() {
		   if(pspBookingContainer == null) {
		      pspBookingContainer = new PSPBookingContainer();
		   }
		   DashLoggerUtil.log(Level.DEBUG,"created/retrieved singleton pspBookingContainer:");
		   return pspBookingContainer;
	}
	

//	public List<PSPBooking> getPspBookings() {
//		return pspBookings;
//	}
//
//	public void setPspBookings(List<PSPBooking> pspBookings) {
//		this.pspBookings = pspBookings;
//	}





	public List<PSPBooking> getPspBookingsForTheYear() {
		return pspBookingsForTheYear;
	}

	public void setPspBookingsForTheYear(List<PSPBooking> pspBookingsForTheYear) {
		this.pspBookingsForTheYear = pspBookingsForTheYear;
	}

//	public List<SeatSellSSR> getSeatSellSSRs() {
//		return seatSellSSRs;
//	}
//
//	public void setSeatSellSSRs(List<SeatSellSSR> seatSellSSRs) {
//		this.seatSellSSRs = seatSellSSRs;
//	}

	public static Long getLastLoaded() {
		return lastLoaded;
	}

	public static void setLastLoaded(Long lastLoaded) {
		PSPBookingContainer.lastLoaded = lastLoaded;
	}

	public static void flushModel(){
		pspBookingContainer = null;
//		this.pspBookingsNew = null;
//	    this.pspBookingsForTheYearNew = null;
		
//		delays = null;
		//lastLoaded flush??
	}

	
}
