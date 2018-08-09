package com.emirates.dash.vo;

public class MapEventVO {

	//to be decided what to show in UI
	private String pos;
	private String posName;
	private Integer transactions;
	private Integer Business;
	private Integer economy;
	private Integer first;
	private Double latitude;
	private Double longitude;
	

	public String getPosName() {
		return posName;
	}
	public void setPosName(String posName) {
		this.posName = posName;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public Integer getTransactions() {
		return transactions;
	}
	public void setTransactions(Integer transactions) {
		this.transactions = transactions;
	}
	public Integer getBusiness() {
		return Business;
	}
	public void setBusiness(Integer business) {
		Business = business;
	}
	public Integer getEconomy() {
		return economy;
	}
	public void setEconomy(Integer economy) {
		this.economy = economy;
	}
	public Integer getFirst() {
		return first;
	}
	public void setFirst(Integer first) {
		this.first = first;
	}
	
	
}
