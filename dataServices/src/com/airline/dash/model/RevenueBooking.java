package com.emirates.dash.model;

public class RevenueBooking {

    private String currencyCode;
	private String baseFare;
	private String bookedDate;
	private String totalPassengers;
	private String cabinClass;
	private String pos_Cntry;
	
	
	
	
	
	public String getPos_Cntry() {
		return pos_Cntry;
	}
	public void setPos_Cntry(String pos_Cntry) {
		this.pos_Cntry = pos_Cntry;
	}
	public String getCabinClass() {
		return cabinClass;
	}
	public void setCabinClass(String cabinClass) {
		this.cabinClass = cabinClass;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getBaseFare() {
		return baseFare;
	}
	public void setBaseFare(String baseFare) {
		this.baseFare = baseFare;
	}
	public String getBookedDate() {
		return bookedDate;
	}
	public void setBookedDate(String bookedDate) {
		this.bookedDate = bookedDate;
	}
	public String getTotalPassengers() {
		return totalPassengers;
	}
	public void setTotalPassengers(String totalPassengers) {
		this.totalPassengers = totalPassengers;
	}
	
	
}
