package com.emirates.dash.vo;

public class MonthlyVO {

	private String monthYear;
	private Integer seats;
	private Double seatPercentageOverYear;
	
	
	
	public Double getSeatPercentageOverYear() {
		return seatPercentageOverYear;
	}
	public void setSeatPercentageOverYear(Double seatPercentageOverYear) {
		this.seatPercentageOverYear = seatPercentageOverYear;
	}
	public String getMonthYear() {
		return monthYear;
	}
	public void setMonthYear(String monthYear) {
		this.monthYear = monthYear;
	}
	public Integer getSeats() {
		return seats;
	}
	public void setSeats(Integer seats) {
		this.seats = seats;
	}
//	@Override
//	public int compareTo(MonthlyVO o) {
//
//		if(o.getSeats()>this.getSeats()){
//			return 1;
//		}
//		
//		return 0;
//	}
	
	
}
