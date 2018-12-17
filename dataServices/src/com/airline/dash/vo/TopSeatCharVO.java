package com.emirates.dash.vo;

public class TopSeatCharVO {

	private String seatCharacteristic;
	private Integer seats;
	private Double seatsPercentage;
	private Double revenue;
	private Double revenuePercentage;
	
	public String getSeatCharacteristic() {
		return seatCharacteristic;
	}
	public void setSeatCharacteristic(String seatCharacteristic) {
		this.seatCharacteristic = seatCharacteristic;
	}
	public Integer getSeats() {
		return seats;
	}
	public void setSeats(Integer seats) {
		this.seats = seats;
	}
	public Double getSeatsPercentage() {
		return seatsPercentage;
	}
	public void setSeatsPercentage(Double seatsPercentage) {
		this.seatsPercentage = seatsPercentage;
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

