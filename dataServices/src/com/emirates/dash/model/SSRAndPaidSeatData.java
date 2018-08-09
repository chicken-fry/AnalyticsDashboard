package com.emirates.dash.model;

import java.math.BigDecimal;
import java.util.Date;

public class SSRAndPaidSeatData {

	private String cabinClass;
	private String seatCharacteristics;
	private String baseFare;
	private String exchangedFare;
	private String fareBrand;
	private String currencyCode;
	private int totalPax;
	private BigDecimal ofwFare;
	private BigDecimal childFare;
	private BigDecimal adultFare;
	private BigDecimal teenagerFare;
	
	private String channel;
	private String posCntry;
	private String origin;
	private String destination;
	private String pnr;
	private Date depDate;
	
	private Date transDate;
	
	private BigDecimal total;
	private BigDecimal totalTax;
	private BigDecimal tax;
	
	private String skywardsID;
	
	
	
	

	public BigDecimal getTax() {
		return tax;
	}
	public void setTax(BigDecimal tax) {
		this.tax = tax;
	}
	public String getSkywardsID() {
		return skywardsID;
	}
	public void setSkywardsID(String skywardsID) {
		this.skywardsID = skywardsID;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public BigDecimal getTotalTax() {
		return totalTax;
	}
	public void setTotalTax(BigDecimal totalTax) {
		this.totalTax = totalTax;
	}
	public Date getDepDate() {
		return depDate;
	}
	public void setDepDate(Date depDate) {
		this.depDate = depDate;
	}
	public Date getTransDate() {
		return transDate;
	}
	public void setTransDate(Date transDate) {
		this.transDate = transDate;
	}
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
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getPosCntry() {
		return posCntry;
	}
	public void setPosCntry(String posCntry) {
		this.posCntry = posCntry;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getPnr() {
		return pnr;
	}
	public void setPnr(String pnr) {
		this.pnr = pnr;
	}
	
	

	
}
