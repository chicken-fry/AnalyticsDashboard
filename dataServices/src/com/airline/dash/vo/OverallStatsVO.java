package com.emirates.dash.vo;

import java.util.List;
import java.util.Map;

public class OverallStatsVO {

	private Integer seats;
	private Double revenue;
	private Double change;
//	private Map<Integer,Integer> hourlyBreakdown;
	private List<Integer> hourlyBreakdown;
	
	
	
	
	public List<Integer> getHourlyBreakdown() {
		return hourlyBreakdown;
	}
	public void setHourlyBreakdown(List<Integer> hourlyBreakdown) {
		this.hourlyBreakdown = hourlyBreakdown;
	}
	public Double getChange() {
		return change;
	}
	public void setChange(Double change) {
		this.change = change;
	}
//	public Map<Integer, Integer> getHourlyBreakdown() {
//		return hourlyBreakdown;
//	}
//	public void setHourlyBreakdown(Map<Integer, Integer> hourlyBreakdown) {
//		this.hourlyBreakdown = hourlyBreakdown;
//	}
	public Integer getSeats() {
		return seats;
	}
	public void setSeats(Integer seats) {
		this.seats = seats;
	}
	public Double getRevenue() {
		return revenue;
	}
	public void setRevenue(Double revenue) {
		this.revenue = revenue;
	}
	
	
}
