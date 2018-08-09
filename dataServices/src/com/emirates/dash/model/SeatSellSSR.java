package com.emirates.dash.model;

import java.math.BigDecimal;

public class SeatSellSSR {
	
//	private String flightNo;
//	private String origin;
//	private String destination;
	private String cabinClass;
//	private String segmentStatus;
	private String seatCharacteristics;
//	private String pnr;
//	private String seatNo;
	private String baseFare;
	private String exchangedFare;
	private String fareBrand;
	private String currencyCode;
	private int totalPax;
	private BigDecimal ofwFare;
	private BigDecimal childFare;
	private BigDecimal adultFare;
	private BigDecimal teenagerFare;
	

//	P.CHANNEL, P.POS_CNTRY, P.ORIGIN, P.DESTINATION, P.PNR
	
	
	


	public int getTotalPax() {
		return totalPax;
	}
	public void setTotalPax(int totalPax) {
		this.totalPax = totalPax;
	}
	public BigDecimal getOfwFare() {
		return ofwFare;
	}
	public void setOfwFare(BigDecimal ofwFare) {
		this.ofwFare = ofwFare;
	}
	public BigDecimal getChildFare() {
		return childFare;
	}
	public void setChildFare(BigDecimal childFare) {
		this.childFare = childFare;
	}
	public BigDecimal getAdultFare() {
		return adultFare;
	}
	public void setAdultFare(BigDecimal adultFare) {
		this.adultFare = adultFare;
	}
	public BigDecimal getTeenagerFare() {
		return teenagerFare;
	}
	public void setTeenagerFare(BigDecimal teenagerFare) {
		this.teenagerFare = teenagerFare;
	}
	//	public String getFlightNo() {
//		return flightNo;
//	}
//	public void setFlightNo(String flightNo) {
//		this.flightNo = flightNo;
//	}
	public String getCabinClass() {
		return cabinClass;
	}
	public void setCabinClass(String cabinClass) {
		this.cabinClass = cabinClass;
	}
	public String getSeatCharacteristics() {
		return seatCharacteristics;
	}
	public void setSeatCharacteristics(String seatCharacteristics) {
		this.seatCharacteristics = seatCharacteristics;
	}
	public String getBaseFare() {
		return baseFare;
	}
	public void setBaseFare(String baseFare) {
		this.baseFare = baseFare;
	}
	public String getExchangedFare() {
		return exchangedFare;
	}
	public void setExchangedFare(String exchangedFare) {
		this.exchangedFare = exchangedFare;
	}
	public String getFareBrand() {
		return fareBrand;
	}
	public void setFareBrand(String fareBrand) {
		this.fareBrand = fareBrand;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	
	
	

}
