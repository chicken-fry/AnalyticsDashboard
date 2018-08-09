package com.emirates.dash.model;

import java.math.BigDecimal;
import java.util.Date;

//reflection of DB table
public class PSPBooking {

	private int id;
	private String pnr;
	private String transactionID;
	private String sessionId;
	private String posCntry;
	private String routing;
	private String itineraryOND;
	private String currencyCode;
	private String skywardsID;
	private String skywardsTier;
	private String origin;
	private String destination;
	private Date depDate;
	private Date arrDate;
	private String flightNo;
	private int totalPax;
	private int noOfAdults;
	private BigDecimal adultTotal;
	private BigDecimal adultTax;
	private BigDecimal adultFare;


	private int noofChild;
	private BigDecimal childTotal;
	private BigDecimal childTax;
	private BigDecimal childFare;

	private int noOfTeenager;
	private BigDecimal teenagerTotal;
	private BigDecimal teenagerTax;
	private BigDecimal teenagerFare;

	private int noOfOfw;
	private BigDecimal ofwTotal;
	private BigDecimal ofwTax;
	private BigDecimal ofwFare;	
	
	private BigDecimal total;
	private BigDecimal totalTax;

	private String paymentMethod;
	private String paymentOption;
	private String emdNumber;
	private String emailID;
	private String transStatus;
	private String errorMsg;
	private String siteCountry;
	private String siteLang;
	private String source;
	private String module;
	private String channel;
	private String serverIP;
	private String clientIP;
	private String browserType;
	private String orderCode;
	
	private String bookingSource;
	private Date pnrBookedDate;
	private Date ticketedDateGMT;
	private String airCraftType;
	
	private Date transDate;
	
	
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPnr() {
		return pnr;
	}
	public void setPnr(String pnr) {
		this.pnr = pnr;
	}
	public String getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getPosCntry() {
		return posCntry;
	}
	public void setPosCntry(String posCntry) {
		this.posCntry = posCntry;
	}
	public String getRouting() {
		return routing;
	}
	public void setRouting(String routing) {
		this.routing = routing;
	}
	public String getItineraryOND() {
		return itineraryOND;
	}
	public void setItineraryOND(String itineraryOND) {
		this.itineraryOND = itineraryOND;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getSkywardsID() {
		return skywardsID;
	}
	public void setSkywardsID(String skywardsID) {
		this.skywardsID = skywardsID;
	}
	public String getSkywardsTier() {
		return skywardsTier;
	}
	public void setSkywardsTier(String skywardsTier) {
		this.skywardsTier = skywardsTier;
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
	
	public String getFlightNo() {
		return flightNo;
	}
	public void setFlightNo(String flightNo) {
		this.flightNo = flightNo;
	}
	public int getTotalPax() {
		return totalPax;
	}
	public void setTotalPax(int totalPax) {
		this.totalPax = totalPax;
	}
	public int getNoOfAdults() {
		return noOfAdults;
	}
	public void setNoOfAdults(int noOfAdults) {
		this.noOfAdults = noOfAdults;
	}
	public int getNoofChild() {
		return noofChild;
	}
	public void setNoofChild(int noofChild) {
		this.noofChild = noofChild;
	}
	public int getNoOfTeenager() {
		return noOfTeenager;
	}
	public void setNoOfTeenager(int noOfTeenager) {
		this.noOfTeenager = noOfTeenager;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getPaymentOption() {
		return paymentOption;
	}
	public void setPaymentOption(String paymentOption) {
		this.paymentOption = paymentOption;
	}
	public String getEmdNumber() {
		return emdNumber;
	}
	public void setEmdNumber(String emdNumber) {
		this.emdNumber = emdNumber;
	}
	public String getEmailID() {
		return emailID;
	}
	public void setEmailID(String emailID) {
		this.emailID = emailID;
	}
	public String getTransStatus() {
		return transStatus;
	}
	public void setTransStatus(String transStatus) {
		this.transStatus = transStatus;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getSiteCountry() {
		return siteCountry;
	}
	public void setSiteCountry(String siteCountry) {
		this.siteCountry = siteCountry;
	}
	public String getSiteLang() {
		return siteLang;
	}
	public void setSiteLang(String siteLang) {
		this.siteLang = siteLang;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getServerIP() {
		return serverIP;
	}
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	public String getClientIP() {
		return clientIP;
	}
	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}
	public String getBrowserType() {
		return browserType;
	}
	public void setBrowserType(String browserType) {
		this.browserType = browserType;
	}
	public BigDecimal getAdultTotal() {
		return adultTotal;
	}
	public void setAdultTotal(BigDecimal adultTotal) {
		this.adultTotal = adultTotal;
	}
	public BigDecimal getAdultTax() {
		return adultTax;
	}
	public void setAdultTax(BigDecimal adultTax) {
		this.adultTax = adultTax;
	}
	public BigDecimal getAdultFare() {
		return adultFare;
	}
	public void setAdultFare(BigDecimal adultFare) {
		this.adultFare = adultFare;
	}
	public BigDecimal getChildTotal() {
		return childTotal;
	}
	public void setChildTotal(BigDecimal childTotal) {
		this.childTotal = childTotal;
	}
	public BigDecimal getChildTax() {
		return childTax;
	}
	public void setChildTax(BigDecimal childTax) {
		this.childTax = childTax;
	}
	public BigDecimal getChildFare() {
		return childFare;
	}
	public void setChildFare(BigDecimal childFare) {
		this.childFare = childFare;
	}
	public BigDecimal getTeenagerTotal() {
		return teenagerTotal;
	}
	public void setTeenagerTotal(BigDecimal teenagerTotal) {
		this.teenagerTotal = teenagerTotal;
	}
	public BigDecimal getTeenagerTax() {
		return teenagerTax;
	}
	public void setTeenagerTax(BigDecimal teenagerTax) {
		this.teenagerTax = teenagerTax;
	}
	public BigDecimal getTeenagerFare() {
		return teenagerFare;
	}
	public void setTeenagerFare(BigDecimal teenagerFare) {
		this.teenagerFare = teenagerFare;
	}
	public int getNoOfOfw() {
		return noOfOfw;
	}
	public void setNoOfOfw(int noOfOfw) {
		this.noOfOfw = noOfOfw;
	}
	public BigDecimal getOfwTotal() {
		return ofwTotal;
	}
	public void setOfwTotal(BigDecimal ofwTotal) {
		this.ofwTotal = ofwTotal;
	}
	public BigDecimal getOfwTax() {
		return ofwTax;
	}
	public void setOfwTax(BigDecimal ofwTax) {
		this.ofwTax = ofwTax;
	}
	public BigDecimal getOfwFare() {
		return ofwFare;
	}
	public void setOfwFare(BigDecimal ofwFare) {
		this.ofwFare = ofwFare;
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
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getBookingSource() {
		return bookingSource;
	}
	public void setBookingSource(String bookingSource) {
		this.bookingSource = bookingSource;
	}

	public String getAirCraftType() {
		return airCraftType;
	}
	public void setAirCraftType(String airCraftType) {
		this.airCraftType = airCraftType;
	}
	public Date getDepDate() {
		return depDate;
	}
	public void setDepDate(Date depDate) {
		this.depDate = depDate;
	}
	public Date getArrDate() {
		return arrDate;
	}
	public void setArrDate(Date arrDate) {
		this.arrDate = arrDate;
	}
	public Date getPnrBookedDate() {
		return pnrBookedDate;
	}
	public void setPnrBookedDate(Date pnrBookedDate) {
		this.pnrBookedDate = pnrBookedDate;
	}
	public Date getTicketedDateGMT() {
		return ticketedDateGMT;
	}
	public void setTicketedDateGMT(Date ticketedDateGMT) {
		this.ticketedDateGMT = ticketedDateGMT;
	}
	public Date getTransDate() {
		return transDate;
	}
	public void setTransDate(Date transDate) {
		this.transDate = transDate;
	}
	
	
}
