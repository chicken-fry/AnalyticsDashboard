package com.emirates.dash.vo;

public class TopHaulVO {

	private String haulType;
	private Integer bookings;
	private Double bookingsPercentage;
	private Double revenue;
	private Double revenuePercentage;
	
	public String getHaulType() {
		return haulType;
	}
	public void setHaulType(String haulType) {
		this.haulType = haulType;
	}
	public Integer getBookings() {
		return bookings;
	}
	public void setBookings(Integer bookings) {
		this.bookings = bookings;
	}
	public Double getBookingsPercentage() {
		return bookingsPercentage;
	}
	public void setBookingsPercentage(Double bookingsPercentage) {
		this.bookingsPercentage = bookingsPercentage;
	}
	public Double getRevenue() {
		return revenue;
	}
	public void setRevenue(Double revenue) {
		this.revenue = revenue;
	}
	public Double getRevenuePercentage() {
		return revenuePercentage;
	}
	public void setRevenuePercentage(Double revenuePercentage) {
		this.revenuePercentage = revenuePercentage;
	}
	
	
	
}
