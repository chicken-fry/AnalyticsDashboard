package com.emirates.dash.vo;

//to be generic
public class Top10PosVO {
	
	private String posCountryForDisplay;
	private String posCountryCode;
	private Integer bookings;//or seats
	private Double percentageOfTotal;//usefull in PSP
	private Double revenue;
	private Double percentageOfTotalRevenue;
	private Double change;
	
	
	
	
	public String getPosCountryCode() {
		return posCountryCode;
	}
	public void setPosCountryCode(String posCountryCode) {
		this.posCountryCode = posCountryCode;
	}
	public Double getChange() {
		return change;
	}
	public void setChange(Double change) {
		this.change = change;
	}
	public String getPosCountryForDisplay() {
		return posCountryForDisplay;
	}
	public void setPosCountryForDisplay(String posCountryForDisplay) {
		this.posCountryForDisplay = posCountryForDisplay;
	}
	public Integer getBookings() {
		return bookings;
	}
	public void setBookings(Integer bookings) {
		this.bookings = bookings;
	}
	public Double getPercentageOfTotal() {
		return percentageOfTotal;
	}
	public void setPercentageOfTotal(Double percentageOfTotal) {
		this.percentageOfTotal = percentageOfTotal;
	}
	public Double getRevenue() {
		return revenue;
	}
	public void setRevenue(Double revenue) {
		this.revenue = revenue;
	}
	public Double getPercentageOfTotalRevenue() {
		return percentageOfTotalRevenue;
	}
	public void setPercentageOfTotalRevenue(Double percentageOfTotalRevenue) {
		this.percentageOfTotalRevenue = percentageOfTotalRevenue;
	}
	
	
	

	
	
}
