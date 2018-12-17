package com.emirates.dash.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;

import com.emirates.dash.model.HaulType;
import com.emirates.dash.model.PSPBooking;
import com.emirates.dash.model.PSPBookingContainer;
import com.emirates.dash.model.RevenueBooking;
import com.emirates.dash.model.SSRAndPaidSeatData;
import com.emirates.dash.model.SeatSellSSR;
import com.emirates.dash.utilities.DBConnectionPool;
import com.emirates.dash.utilities.DashConstants;
import com.emirates.dash.utilities.DashLoggerUtil;
import com.emirates.dash.utilities.DashProperties;


public class SQLDataAccess {


	//only using USD bookings since currency converstion service needs to be consumed (from CPG?MRP?TIBCO?)which will require RFC as well
	//TODO Optimize/double check
	//"SELECT TOP 10 * FROM PaidSeat_Booking_Details where TransDate is not null and Origin not in ('CRK') and CurrencyCode = 'USD' order by TransDate desc";
	public static final String PSP_TABLE_MOCKDATA_RETRIEVE = DashProperties.getProperty("PSP_TABLE_MOCKDATA_RETRIEVE");
	//"SELECT TOP 500 * FROM PaidSeat_Booking_Details where TransDate is not null and CurrencyCode = 'USD' order by TransDate desc";	
	public static final String PSP_TABLE_BASE_MOCKDATA_RETRIEVE = DashProperties.getProperty("PSP_TABLE_BASE_MOCKDATA_RETRIEVE");
	//"SELECT currency_code, base_fare, booked_date, total_passengers, cabin_class , pos_city FROM Report_Booking_Details WHERE booking_status = 'Booked' and booking_type NOT IN('smeredeem','redeem','REDEEM') and currency_code ='USD' and booked_date > ? order by booked_date desc";
	public static final String BOOKING_TABLE_MOCKDATA_RETRIEVE = DashProperties.getProperty("BOOKING_TABLE_MOCKDATA_RETRIEVE");
	//"SELECT top 500 CabinClass, seatCharacteristic, base_fare, exchangedFare, fareBrand FROM EMI_QH_SSRSeatSell where isCancelled = '0' and isPaid = 'Yes' order by Updated_Date desc";
	public static final String SEATSELL_TABLE_BASE_MOCKDATA_RETRIEVE = DashProperties.getProperty("SEATSELL_TABLE_BASE_MOCKDATA_RETRIEVE");
	//"SELECT top 10 CabinClass, seatCharacteristic, base_fare, exchangedFare, fareBrand FROM EMI_QH_SSRSeatSell where isCancelled = '0' and isPaid = 'Yes' order by Updated_Date desc";
    public static final String SEATSELL_TABLE_MOCKDATA_RETRIEVE = DashProperties.getProperty("SEATSELL_TABLE_MOCKDATA_RETRIEVE");
    //"SELECT TOP 2000 * FROM PaidSeat_Booking_Details where TransDate is not null and CurrencyCode = 'USD' order by TransDate desc";
    public static final String PSP_TABLE_BASE_MOCKDATA_RETRIEVE_FOR_A_YEAR = DashProperties.getProperty("PSP_TABLE_BASE_MOCKDATA_RETRIEVE_FOR_A_YEAR");
    
    //"SELECT * FROM PaidSeat_Booking_Details where TransDate is not null and CurrencyCode = 'USD' and TransDate > ? order by TransDate asc";
    public static final String PSP_TABLE_RETRIEVE= DashProperties.getProperty("PSP_TABLE_RETRIEVE");
    //"SELECT currency_code, base_fare, booked_date, total_passengers, cabin_class , pos_city FROM Report_Booking_Details WHERE booking_status = 'Booked' and booking_type NOT IN('smeredeem','redeem','REDEEM') and currency_code ='USD' and booked_date > ? order by booked_date asc";
	public static final String BOOKING_TABLE_RETRIEVE = DashProperties.getProperty("BOOKING_TABLE_RETRIEVE");
	//"SELECT CabinClass, seatCharacteristic, base_fare, exchangedFare, fareBrand FROM EMI_QH_SSRSeatSell where isCancelled = '0' and isPaid = 'Yes' and (Created_Date > ? or Updated_Date > ?) order by Updated_Date asc";
    public static final String SEATSELL_TABLE_RETRIEVE = DashProperties.getProperty("SEATSELL_TABLE_RETRIEVE");
    //"SELECT * FROM PaidSeat_Booking_Details where TransDate is not null and CurrencyCode = 'USD' and TransDate > ? order by TransDate asc";
    public static final String PSP_TABLE_BASE_RETRIEVE_FOR_A_YEAR = DashProperties.getProperty("PSP_TABLE_BASE_RETRIEVE_FOR_A_YEAR");
    //"SELECT * FROM M_OD_Country";
    public static final String HAUL_TYPES_TABLE_RETRIEVE = DashProperties.getProperty("HAUL_TYPES_TABLE_RETRIEVE");
    //"SELECT * FROM M_CURRENCYRATE";
    public static final String CURRENCY_RATES_TABLE_RETRIEVE = DashProperties.getProperty("CURRENCY_RATES_TABLE_RETRIEVE");
    
    public static final String TABLE_RETRIEVE_NEW_DATEBASED = DashProperties.getProperty("TABLE_RETRIEVE_NEW_DATEBASED");
    
    public static final String TABLE_RETRIEVE_NEW_LIVE = DashProperties.getProperty("TABLE_RETRIEVE_NEW_LIVE");
    
    public static final String TEST_QUERY = "select count(*) as count from (\r\n" + 
    		"SELECT distinct Q1.PAXNAME, Q1.EMD_Number, Q1.Updated_Date,Q1.CabinClass, Q1.seatCharacteristic, Q1.base_fare, Q1.exchangedFare, Q1.fareBrand, P.CurrencyCode, P.OFWFARE, P.TEENAGERFARE,P.ADULTFARE,P.CHILDFARE, P.TOTALPAX, P.CHANNEL, P.ORIGIN, P.DESTINATION, P.PNR, P.TRANSDATE, P.POS_CNTRY, P.DEPARTUREDATE, P.TOTAL, P.TOTALTAX, P.SKYWARDSID FROM T_EMI_QH_SSRSEATSELL_DASH Q1 inner join T_PAID_SEAT_BOOK_DETAILS_DASH P on\r\n" + 
    		"(P.transactionid=Q1.transactionid \r\n" + 
    		"AND Q1.EMD_NUMBER is not null \r\n" +
    		"AND P.PNR=Q1.PNR \r\n" + 
    		"AND P.Flightno=Cast(Q1.FlightNo as int)\r\n" + 
    		"AND P.Origin=Q1.Origin\r\n" + 
    		"AND P.Destination=Q1.Destination \r\n" + 
    		"AND P.DepartureDate=Q1.DepartureDate \r\n" + 
    		"AND Q1.ispaid='Yes' \r\n" + 
    		"AND (Q1.Created_Date >= ? or Q1.Updated_Date >= ?) \r\n" + 
    		"AND P.TransDate is not null \r\n" + 
    		"and P.TransDate > ? )\r\n" + 
    		"where P.TRANSSTATUS = 'Completed' order by P.TransDate asc)";
    
    
	public static List<SeatSellSSR> pullSeatSellMockData(int baseOrDelta) {
		

	    
		Connection connection = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;
		SeatSellSSR seatSellSSR = null;
		List<SeatSellSSR> seatSellSSRs = new ArrayList<SeatSellSSR>();
		connection = DBConnectionPool.getConnection();
		try {
		  if(baseOrDelta == 0)
		    callableStatement = connection.prepareCall(SEATSELL_TABLE_BASE_MOCKDATA_RETRIEVE);
		  else
			callableStatement = connection.prepareCall(SEATSELL_TABLE_MOCKDATA_RETRIEVE);
		  

		  resultSet = callableStatement.executeQuery();
		  while(resultSet.next()){

			  seatSellSSR = new SeatSellSSR();
			  seatSellSSR.setCabinClass(resultSet.getString("CabinClass"));
			  seatSellSSR.setSeatCharacteristics(resultSet.getString("seatCharacteristic"));
			  seatSellSSR.setBaseFare(resultSet.getString("base_fare"));
			  seatSellSSR.setExchangedFare(resultSet.getString("exchangedFare"));
			  seatSellSSR.setFareBrand(resultSet.getString("fareBrand"));
			  
			  seatSellSSRs.add(seatSellSSR);
		  }
	  
		} catch (SQLException e) {
			DashLoggerUtil.log(Level.ERROR, "MOCK data pull failed sql exception" + e.getLocalizedMessage());
			e.printStackTrace();
		}finally{
			DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
		}
		
		
		return seatSellSSRs;

	 }
	

	
    
    
	public static List<PSPBooking> pullPSPMockData(){
			
		Connection connection = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;
		PSPBooking pspBooking = null;
		List<PSPBooking> pspBookingsList = new ArrayList<PSPBooking>();
		connection = DBConnectionPool.getConnection();
		try {
		  callableStatement = connection.prepareCall(PSP_TABLE_MOCKDATA_RETRIEVE);
		  resultSet = callableStatement.executeQuery();
		  while(resultSet.next()){
			  pspBooking = new PSPBooking();
			  pspBooking.setAdultFare(resultSet.getBigDecimal("AdultFare"));
			  pspBooking.setAdultTax(resultSet.getBigDecimal("AdultTax"));
			  pspBooking.setAdultTotal(resultSet.getBigDecimal("AdultTotal"));
			  pspBooking.setAirCraftType(resultSet.getString("Aircrafttype"));
			  pspBooking.setArrDate(resultSet.getDate("ArrivalDate"));//getDate().toString()
			  pspBooking.setBookingSource(resultSet.getString("BookingSource"));
			  pspBooking.setBrowserType(resultSet.getString("BrowserType"));
			  pspBooking.setChannel(resultSet.getString("Channel"));
			  pspBooking.setChildFare(resultSet.getBigDecimal("ChildFare"));
			  pspBooking.setChildTax(resultSet.getBigDecimal("ChildTax"));
			  pspBooking.setChildTotal(resultSet.getBigDecimal("ChildTotal"));
			  pspBooking.setClientIP(resultSet.getString("ClientIP"));
			  pspBooking.setCurrencyCode(resultSet.getString("CurrencyCode"));
			  pspBooking.setDepDate(resultSet.getDate("DepartureDate"));
			  pspBooking.setDestination(resultSet.getString("Destination"));
			  pspBooking.setEmailID(resultSet.getString("EmailID"));
			  pspBooking.setEmdNumber(resultSet.getString("EMD_Numbers"));
			  pspBooking.setErrorMsg(resultSet.getString("ErrorMsg"));
			  pspBooking.setFlightNo(resultSet.getString("FlightNo"));
			  pspBooking.setId(resultSet.getInt("ID"));
			  pspBooking.setItineraryOND(resultSet.getString("ItineraryOND"));
			  pspBooking.setModule(resultSet.getString("Module"));
			  pspBooking.setNoOfAdults(resultSet.getInt("NumberOfAdults"));
			  pspBooking.setNoofChild(resultSet.getInt("NumberOfChild"));
			  pspBooking.setNoOfOfw(resultSet.getInt("NumberOfOFW"));
			  pspBooking.setNoOfTeenager(resultSet.getInt("NumberOfTeenager"));
			  pspBooking.setOfwFare(resultSet.getBigDecimal("OFWFare"));
			  pspBooking.setOfwTax(resultSet.getBigDecimal("OFWTax"));
			  pspBooking.setOfwTotal(resultSet.getBigDecimal("OFWTotal"));
			  pspBooking.setOrderCode(resultSet.getString("OrderCode"));
			  pspBooking.setOrigin(resultSet.getString("Origin"));
			  pspBooking.setPaymentMethod(resultSet.getString("Payment_Method"));
			  pspBooking.setPaymentOption(resultSet.getString("Payment_Option"));
			  pspBooking.setPnr(resultSet.getString("PNR"));
			  pspBooking.setPnrBookedDate(resultSet.getDate("BookeddateGMT"));
			  pspBooking.setPosCntry(resultSet.getString("Pos_Cntry"));
			  pspBooking.setRouting(resultSet.getString("Routing"));
			  pspBooking.setServerIP(resultSet.getString("ServerIP"));
			  pspBooking.setSessionId(resultSet.getString("SessionID"));
			  pspBooking.setSiteCountry(resultSet.getString("Site_Cntry"));
			  pspBooking.setSiteLang(resultSet.getString("Site_Lang"));
			  pspBooking.setSkywardsID(resultSet.getString("SkywardsID"));
			  pspBooking.setSkywardsTier(resultSet.getString("SkywardsTier"));
			  pspBooking.setSource(resultSet.getString("Source"));
			  pspBooking.setTeenagerFare(resultSet.getBigDecimal("TeenagerFare"));
			  pspBooking.setTeenagerTax(resultSet.getBigDecimal("TeenagerTax"));
			  pspBooking.setTeenagerTotal(resultSet.getBigDecimal("TeenagerTotal"));
			  pspBooking.setTicketedDateGMT(resultSet.getDate("TicketeddateGMT"));
			  pspBooking.setTotal(resultSet.getBigDecimal("Total"));
			  pspBooking.setTotalPax(resultSet.getInt("TotalPax"));
			  pspBooking.setTotalTax(resultSet.getBigDecimal("TotalTax"));
			  pspBooking.setTransactionID(resultSet.getString("TransactionID"));
			  pspBooking.setTransStatus(resultSet.getString("TransStatus"));
			  pspBooking.setTransDate(resultSet.getDate("TransDate"));
			  pspBookingsList.add(pspBooking);		  
		  }
		
		  
		} catch (SQLException e) {
			DashLoggerUtil.log(Level.ERROR, "MOCK data pull failed sql exception" + e.getLocalizedMessage());
			e.printStackTrace();
		}finally{
			DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
		}
		
		
		return pspBookingsList;
		
	} 
	


	//pulls top 500 transactions
	//TODO Need to do for all OVERLAYS and eventually all modes
	public static List<PSPBooking> pullPSPMockBaseData() {
		
		
		Connection connection = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;
		PSPBooking pspBooking = null;
		List<PSPBooking> pspBookingsList = new ArrayList<PSPBooking>();
		connection = DBConnectionPool.getConnection();
		try {
		  callableStatement = connection.prepareCall(PSP_TABLE_BASE_MOCKDATA_RETRIEVE);
		  resultSet = callableStatement.executeQuery();
		  while(resultSet.next()){
			  pspBooking = new PSPBooking();
			  pspBooking.setAdultFare(resultSet.getBigDecimal("AdultFare"));
			  pspBooking.setAdultTax(resultSet.getBigDecimal("AdultTax"));
			  pspBooking.setAdultTotal(resultSet.getBigDecimal("AdultTotal"));
			  pspBooking.setAirCraftType(resultSet.getString("Aircrafttype"));
			  pspBooking.setArrDate(resultSet.getDate("ArrivalDate"));//getDate().toString()
			  pspBooking.setBookingSource(resultSet.getString("BookingSource"));
			  pspBooking.setBrowserType(resultSet.getString("BrowserType"));
			  pspBooking.setChannel(resultSet.getString("Channel"));
			  pspBooking.setChildFare(resultSet.getBigDecimal("ChildFare"));
			  pspBooking.setChildTax(resultSet.getBigDecimal("ChildTax"));
			  pspBooking.setChildTotal(resultSet.getBigDecimal("ChildTotal"));
			  pspBooking.setClientIP(resultSet.getString("ClientIP"));
			  pspBooking.setCurrencyCode(resultSet.getString("CurrencyCode"));
			  pspBooking.setDepDate(resultSet.getDate("DepartureDate"));
			  pspBooking.setDestination(resultSet.getString("Destination"));
			  pspBooking.setEmailID(resultSet.getString("EmailID"));
			  pspBooking.setEmdNumber(resultSet.getString("EMD_Numbers"));
			  pspBooking.setErrorMsg(resultSet.getString("ErrorMsg"));
			  pspBooking.setFlightNo(resultSet.getString("FlightNo"));
			  pspBooking.setId(resultSet.getInt("ID"));
			  pspBooking.setItineraryOND(resultSet.getString("ItineraryOND"));
			  pspBooking.setModule(resultSet.getString("Module"));
			  pspBooking.setNoOfAdults(resultSet.getInt("NumberOfAdults"));
			  pspBooking.setNoofChild(resultSet.getInt("NumberOfChild"));
			  pspBooking.setNoOfOfw(resultSet.getInt("NumberOfOFW"));
			  pspBooking.setNoOfTeenager(resultSet.getInt("NumberOfTeenager"));
			  pspBooking.setOfwFare(resultSet.getBigDecimal("OFWFare"));
			  pspBooking.setOfwTax(resultSet.getBigDecimal("OFWTax"));
			  pspBooking.setOfwTotal(resultSet.getBigDecimal("OFWTotal"));
			  pspBooking.setOrderCode(resultSet.getString("OrderCode"));
			  pspBooking.setOrigin(resultSet.getString("Origin"));
			  pspBooking.setPaymentMethod(resultSet.getString("Payment_Method"));
			  pspBooking.setPaymentOption(resultSet.getString("Payment_Option"));
			  pspBooking.setPnr(resultSet.getString("PNR"));
			  pspBooking.setPnrBookedDate(resultSet.getDate("BookeddateGMT"));
			  pspBooking.setPosCntry(resultSet.getString("Pos_Cntry"));
			  pspBooking.setRouting(resultSet.getString("Routing"));
			  pspBooking.setServerIP(resultSet.getString("ServerIP"));
			  pspBooking.setSessionId(resultSet.getString("SessionID"));
			  pspBooking.setSiteCountry(resultSet.getString("Site_Cntry"));
			  pspBooking.setSiteLang(resultSet.getString("Site_Lang"));
			  pspBooking.setSkywardsID(resultSet.getString("SkywardsID"));
			  pspBooking.setSkywardsTier(resultSet.getString("SkywardsTier"));
			  pspBooking.setSource(resultSet.getString("Source"));
			  pspBooking.setTeenagerFare(resultSet.getBigDecimal("TeenagerFare"));
			  pspBooking.setTeenagerTax(resultSet.getBigDecimal("TeenagerTax"));
			  pspBooking.setTeenagerTotal(resultSet.getBigDecimal("TeenagerTotal"));
			  pspBooking.setTicketedDateGMT(resultSet.getDate("TicketeddateGMT"));
			  pspBooking.setTotal(resultSet.getBigDecimal("Total"));
			  pspBooking.setTotalPax(resultSet.getInt("TotalPax"));
			  pspBooking.setTotalTax(resultSet.getBigDecimal("TotalTax"));
			  pspBooking.setTransactionID(resultSet.getString("TransactionID"));
			  pspBooking.setTransStatus(resultSet.getString("TransStatus"));
			  pspBooking.setTransDate(resultSet.getDate("TransDate"));
			  pspBookingsList.add(pspBooking);		  
		  }
		
		  
		} catch (SQLException e) {
			DashLoggerUtil.log(Level.ERROR, "MOCK data pull failed sql exception" + e.getLocalizedMessage());
			e.printStackTrace();
		}finally{
			DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
		}
		
		
		return pspBookingsList;
	}




    //pulls bookings after given transaction date
	public static List<RevenueBooking> pullBookingData(Date transDate) {
		

	    java.sql.Date sqlDate = new java.sql.Date(transDate.getTime());
	    
		Connection connection = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;
		RevenueBooking revenueBooking = null;
		List<RevenueBooking> revenueBookingList = new ArrayList<RevenueBooking>();
		connection = DBConnectionPool.getConnection();
		try {
		  callableStatement = connection.prepareCall(BOOKING_TABLE_MOCKDATA_RETRIEVE);
		  callableStatement.setDate(1, sqlDate);
		  resultSet = callableStatement.executeQuery();
		  while(resultSet.next()){

			  revenueBooking = new RevenueBooking();
			  revenueBooking.setCurrencyCode(resultSet.getString("currency_code"));
			  revenueBooking.setBookedDate(resultSet.getString("booked_date"));
			  revenueBooking.setTotalPassengers(resultSet.getString("total_passengers"));
			  revenueBooking.setBaseFare(resultSet.getString("base_fare"));
			  revenueBooking.setCabinClass(resultSet.getString("cabin_class"));
			  revenueBooking.setPos_Cntry(resultSet.getString("pos_city"));
			  
			  revenueBookingList.add(revenueBooking);
		  }
	  
		} catch (SQLException e) {
			DashLoggerUtil.log(Level.ERROR, "pullBookingData data pull failed sql exception" + e.getLocalizedMessage());
			e.printStackTrace();
		}finally{
			DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
		}
		
		
		return revenueBookingList;

	 }
	
	
	
	
	public static List<PSPBooking> pullPSPLiveData(int baseOrDelta, Long lastLoadTime){
		Date javaDateForLogging = null;
		java.sql.Date sqlDate = null;
		//for Date based overlay
		java.sql.Date sqlEndDateBase = null;
		java.sql.Date sqlEndDateDelta = null;
		
		java.sql.Timestamp sqlTimestampInitial = null;
		java.sql.Timestamp sqlTimestampEndBase = null;
		java.sql.Timestamp sqlTimestampEndDelta = null;
		
		if(baseOrDelta==0){ //base
			
			if(DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")||DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)==null) {
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
		//		cal.add(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND,0);
				cal.set(Calendar.MILLISECOND, 0);
				
				Date todayStart = cal.getTime();
				long t = todayStart.getTime();
				sqlTimestampInitial = new java.sql.Timestamp(t);
				DashLoggerUtil.log(Level.INFO, "pulling PSPBooking BASE data after time :::"+todayStart.toString());
				sqlDate = new java.sql.Date(todayStart.getTime());
				
			}else {
				String temp = DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY);
				SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
				Date date = null;
				Date start = null;
			    try {
					date = parser.parse(temp);
				} catch (ParseException e) {
					DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
					e.printStackTrace();
				}
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND,0);
				cal.set(Calendar.MILLISECOND, 0);
				start = cal.getTime();
			    sqlDate = new java.sql.Date(start.getTime());//start date of date based overlay
				long t = start.getTime();
				sqlTimestampInitial = new java.sql.Timestamp(t);
			    
			    Calendar cal2 = Calendar.getInstance();
			    Date dateToday = new Date();//current time today
			    cal2.setTime(dateToday);
			    
			    Calendar cal3 = Calendar.getInstance();
			    cal3.setTime(date);//15 nov
				cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
				cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
				cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
				cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
				Date endDateBase = cal3.getTime();
				long t2 = endDateBase.getTime();
				sqlTimestampEndBase = new java.sql.Timestamp(t2);
				sqlEndDateBase = new java.sql.Date(endDateBase.getTime());

			}
		}else{ // delta
			
			if(DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")||DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)==null) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(lastLoadTime);
				Date deltaDate = cal.getTime();
				long t = deltaDate.getTime();
				sqlTimestampInitial = new java.sql.Timestamp(t);
				DashLoggerUtil.log(Level.INFO, "pulling PSPBooking data after time :::"+deltaDate.toString());
				sqlDate = new java.sql.Date(deltaDate.getTime());
			}else {
				
				String temp = DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY);
				SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
				Date date = null;
			    try {
					date = parser.parse(temp);
				} catch (ParseException e) {
					DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
					e.printStackTrace();
				}
			
				
			    Calendar cal2 = Calendar.getInstance();
			    Date dateToday = new Date();//current time today
			    cal2.setTime(dateToday);
			    
			    Calendar cal3 = Calendar.getInstance();
			    cal3.setTime(date);//15 nov
				cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
				cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
				cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
				cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
				Date startDateDelta = cal3.getTime();
				long t = startDateDelta.getTime();
				sqlTimestampInitial = new java.sql.Timestamp(t);
				sqlDate = new java.sql.Date(startDateDelta.getTime());
				
				cal3.add(Calendar.MINUTE,1);
				Date endDateDelta = cal3.getTime();
				long t2 = endDateDelta.getTime();
				sqlTimestampEndDelta = new java.sql.Timestamp(t2);
				sqlEndDateDelta = new java.sql.Date(endDateDelta.getTime());
				
			}
		}
		Connection connection = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;
		PSPBooking pspBooking = null;
		List<PSPBooking> pspBookingsList = new ArrayList<PSPBooking>();
		connection = DBConnectionPool.getConnection();
		
		try {
		  javaDateForLogging = new Date(sqlTimestampInitial.getTime());
		  DashLoggerUtil.log(Level.INFO, "pulling paidSeatBooking delta data from time :::"+javaDateForLogging.toString());
		  callableStatement = connection.prepareCall(PSP_TABLE_RETRIEVE);
		  callableStatement.setTimestamp(1, sqlTimestampInitial);
		  //REMOVE AFTER TESTING? - START
		  if(!DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")&& DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)!=null) {
			  if(baseOrDelta==0){
				  javaDateForLogging = new Date(sqlTimestampEndBase.getTime());
				  DashLoggerUtil.log(Level.INFO, "pulling paidSeatBooking base data till time :::"+javaDateForLogging.toString());
				  callableStatement.setTimestamp(2, sqlTimestampEndBase);//base
			  }else {
				  javaDateForLogging = new Date(sqlTimestampEndDelta.getTime());
				  DashLoggerUtil.log(Level.INFO, "pulling paidSeatBooking delta data till time :::"+javaDateForLogging.toString());
				  callableStatement.setTimestamp(2, sqlTimestampEndDelta);//delta
			  }
		  }
		  //REMOVE AFTER TESTING? - END
		  
		  resultSet = callableStatement.executeQuery();
		  while(resultSet.next()){
			  pspBooking = new PSPBooking();
			  pspBooking.setAdultFare(resultSet.getString("AdultFare")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("AdultFare")));
//			  pspBooking.setAdultTax(resultSet.getBigDecimal("AdultTax"));
//			  pspBooking.setAdultTotal(resultSet.getBigDecimal("AdultTotal"));
//			  pspBooking.setAirCraftType(resultSet.getString("Aircrafttype"));
//			  pspBooking.setArrDate(resultSet.getDate("ArrivalDate"));//getDate().toString()
//			  pspBooking.setBookingSource(resultSet.getString("BookingSource"));
//			  pspBooking.setBrowserType(resultSet.getString("BrowserType"));
			  pspBooking.setChannel(resultSet.getString("Channel"));

			  pspBooking.setChildFare(resultSet.getString("ChildFare")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("ChildFare")));
//			  pspBooking.setChildTax(resultSet.getBigDecimal("ChildTax"));
//			  pspBooking.setChildTotal(resultSet.getBigDecimal("ChildTotal"));
//			  pspBooking.setClientIP(resultSet.getString("ClientIP"));
			  pspBooking.setCurrencyCode(resultSet.getString("CurrencyCode"));
			  pspBooking.setDepDate(resultSet.getDate("DepartureDate"));
			  pspBooking.setDestination(resultSet.getString("Destination"));
//			  pspBooking.setEmailID(resultSet.getString("EmailID"));
//			  pspBooking.setEmdNumber(resultSet.getString("EMD_Numbers"));
//			  pspBooking.setErrorMsg(resultSet.getString("ErrorMsg"));
//			  pspBooking.setFlightNo(resultSet.getString("FlightNo"));
//			  pspBooking.setId(resultSet.getInt("ID"));
			  pspBooking.setItineraryOND(resultSet.getString("ItineraryOND"));
//			  pspBooking.setModule(resultSet.getString("Module"));
			  pspBooking.setNoOfAdults(resultSet.getInt("NumberOfAdults"));
			  pspBooking.setNoofChild(resultSet.getInt("NumberOfChild"));
			  pspBooking.setNoOfOfw(resultSet.getInt("NumberOfOFW"));
			  pspBooking.setNoOfTeenager(resultSet.getInt("NumberOfTeenager"));
			  pspBooking.setOfwFare(resultSet.getString("OFWFare")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("OFWFare")));
//			  pspBooking.setOfwTax(resultSet.getBigDecimal("OFWTax"));
//			  pspBooking.setOfwTotal(resultSet.getBigDecimal("OFWTotal"));
//			  pspBooking.setOrderCode(resultSet.getString("OrderCode"));
			  pspBooking.setOrigin(resultSet.getString("Origin"));
//			  pspBooking.setPaymentMethod(resultSet.getString("Payment_Method"));
//			  pspBooking.setPaymentOption(resultSet.getString("Payment_Option"));
			  pspBooking.setPnr(resultSet.getString("PNR"));
			  pspBooking.setPnrBookedDate(resultSet.getDate("BookeddateGMT"));
			  pspBooking.setPosCntry(resultSet.getString("Pos_Cntry"));
			  pspBooking.setRouting(resultSet.getString("Routing"));
//			  pspBooking.setServerIP(resultSet.getString("ServerIP"));
//			  pspBooking.setSessionId(resultSet.getString("SessionID"));
//			  pspBooking.setSiteCountry(resultSet.getString("Site_Cntry"));
//			  pspBooking.setSiteLang(resultSet.getString("Site_Lang"));
//			  pspBooking.setSkywardsID(resultSet.getString("SkywardsID"));
//			  pspBooking.setSkywardsTier(resultSet.getString("SkywardsTier"));
//			  pspBooking.setSource(resultSet.getString("Source"));
			  pspBooking.setTeenagerFare(resultSet.getString("TeenagerFare")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TeenagerFare")));
//			  pspBooking.setTeenagerTax(resultSet.getBigDecimal("TeenagerTax"));
//			  pspBooking.setTeenagerTotal(resultSet.getBigDecimal("TeenagerTotal"));
			  pspBooking.setTicketedDateGMT(resultSet.getDate("TicketeddateGMT"));
			  pspBooking.setTotal(resultSet.getString("Total")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("Total")));
			  pspBooking.setTotalPax(resultSet.getInt("TotalPax"));
//			  pspBooking.setTotalTax(resultSet.getBigDecimal("TotalTax"));
			  pspBooking.setTransactionID(resultSet.getString("TransactionID"));
			  pspBooking.setTransStatus(resultSet.getString("TransStatus"));
			  pspBooking.setTransDate(resultSet.getDate("TransDate"));
			  pspBookingsList.add(pspBooking);		  
		  }
		
		  
		} catch (SQLException e) {
			DashLoggerUtil.log(Level.ERROR, "pullPSPLiveData data pull failed sql exception" + e.getLocalizedMessage());
			e.printStackTrace();
		}finally{
			DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
		}
		
		
		return pspBookingsList;

	}

	//Data set is the last one year
	/*
	public static List<PSPBooking> pullDataForTheLastYear(boolean mockData) {
			
			Date javaDateForLogging = null;
			java.sql.Date sqlDate = null;
			java.sql.Date endDate = null; //in case of Date based overlay
			//for Oracle
			java.sql.Timestamp sqlTimestampInitial = null;
			java.sql.Timestamp sqlTimestampEnd = null;
			
			if(!mockData){ //base
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
				cal.add(Calendar.MONTH, -12);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND,0);
				cal.set(Calendar.MILLISECOND, 0);
				
				Date lastYearStart = cal.getTime();
				long t = lastYearStart.getTime();
				sqlTimestampInitial = new java.sql.Timestamp(t);
				
				sqlDate = new java.sql.Date(lastYearStart.getTime());
				
				
				if(!DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("") && DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)!=null) {
					String temp = DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY);
					SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
					Date date = null;
				    try {
						date = parser.parse(temp);
					} catch (ParseException e) {
						DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
						e.printStackTrace();
					}
//				    Calendar cal4 = Calendar.getInstance();
//				    cal4.setTime(date);//start of Nov15
//				    cal.set(Calendar.HOUR_OF_DAY, 0);
//					cal.set(Calendar.MINUTE, 0);
//					cal.set(Calendar.SECOND,0);
//					cal.set(Calendar.MILLISECOND, 0);
//					Date YearDataStart
				    
				
				    sqlDate = new java.sql.Date(lastYearStart.getTime());
				    
				    Calendar cal2 = Calendar.getInstance();
				    Date dateToday = new Date();//current time today
				    cal2.setTime(dateToday);
				    
				    Calendar cal3 = Calendar.getInstance();
				    cal3.setTime(date);//15 nov
					cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
					cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
					cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
					cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
					Date endDateHistorical = cal3.getTime();
					long t2 = endDateHistorical.getTime();
					sqlTimestampEnd = new java.sql.Timestamp(t2);
					endDate = new java.sql.Date(endDateHistorical.getTime());
					
					
				}
				
			}
			
			Connection connection = null;
			CallableStatement callableStatement = null;
			ResultSet resultSet = null;
			PSPBooking pspBooking = null;
			List<PSPBooking> pspBookingsList = new ArrayList<PSPBooking>();
			connection = DBConnectionPool.getConnection();
			
			
			
			try {
			  if(mockData)
				  callableStatement = connection.prepareCall(PSP_TABLE_BASE_MOCKDATA_RETRIEVE_FOR_A_YEAR);
			  else{
				  javaDateForLogging = new Date(sqlTimestampInitial.getTime());
				  DashLoggerUtil.log(Level.INFO, "pulling oneYear data after time :::"+javaDateForLogging.toString());
				  callableStatement = connection.prepareCall(PSP_TABLE_RETRIEVE); 
				  callableStatement.setTimestamp(1, sqlTimestampInitial);
				  
				  if(!DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("") && DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)!=null) {
					  
					  javaDateForLogging = new Date(sqlTimestampEnd.getTime());
					  DashLoggerUtil.log(Level.INFO, "pulling oneYear data till:::"+javaDateForLogging.toString());
					  callableStatement.setTimestamp(2, sqlTimestampEnd);
					  
				  }
			  }  
			  resultSet = callableStatement.executeQuery();
			  while(resultSet.next()){
				  pspBooking = new PSPBooking();
				  pspBooking.setAdultFare(resultSet.getString("AdultFare")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("AdultFare")));
//				  pspBooking.setAdultTax(resultSet.getBigDecimal("AdultTax"));
//				  pspBooking.setAdultTotal(resultSet.getBigDecimal("AdultTotal"));
//				  pspBooking.setAirCraftType(resultSet.getString("Aircrafttype"));
//				  pspBooking.setArrDate(resultSet.getDate("ArrivalDate"));//getDate().toString()
//				  pspBooking.setBookingSource(resultSet.getString("BookingSource"));
//				  pspBooking.setBrowserType(resultSet.getString("BrowserType"));
				  pspBooking.setChannel(resultSet.getString("Channel"));
				  pspBooking.setChildFare(resultSet.getString("ChildFare")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("ChildFare")));
//				  pspBooking.setChildTax(resultSet.getBigDecimal("ChildTax"));
//				  pspBooking.setChildTotal(resultSet.getBigDecimal("ChildTotal"));
//				  pspBooking.setClientIP(resultSet.getString("ClientIP"));
				  pspBooking.setCurrencyCode(resultSet.getString("CurrencyCode"));
				  pspBooking.setDepDate(resultSet.getDate("DepartureDate"));
				  pspBooking.setDestination(resultSet.getString("Destination"));
//				  pspBooking.setEmailID(resultSet.getString("EmailID"));
//				  pspBooking.setEmdNumber(resultSet.getString("EMD_Numbers"));
//				  pspBooking.setErrorMsg(resultSet.getString("ErrorMsg"));
//				  pspBooking.setFlightNo(resultSet.getString("FlightNo"));
//				  pspBooking.setId(resultSet.getInt("ID"));
				  pspBooking.setItineraryOND(resultSet.getString("ItineraryOND"));
//				  pspBooking.setModule(resultSet.getString("Module"));
				  pspBooking.setNoOfAdults(resultSet.getInt("NumberOfAdults"));
				  pspBooking.setNoofChild(resultSet.getInt("NumberOfChild"));
				  pspBooking.setNoOfOfw(resultSet.getInt("NumberOfOFW"));
				  pspBooking.setNoOfTeenager(resultSet.getInt("NumberOfTeenager"));
				  pspBooking.setOfwFare(resultSet.getString("OFWFare")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("OFWFare")));
//				  pspBooking.setOfwTax(resultSet.getBigDecimal("OFWTax"));
//				  pspBooking.setOfwTotal(resultSet.getBigDecimal("OFWTotal"));
//				  pspBooking.setOrderCode(resultSet.getString("OrderCode"));
				  pspBooking.setOrigin(resultSet.getString("Origin"));
//				  pspBooking.setPaymentMethod(resultSet.getString("Payment_Method"));
//				  pspBooking.setPaymentOption(resultSet.getString("Payment_Option"));
				  pspBooking.setPnr(resultSet.getString("PNR"));
				  pspBooking.setPnrBookedDate(resultSet.getDate("BookeddateGMT"));
				  pspBooking.setPosCntry(resultSet.getString("Pos_Cntry"));
				  pspBooking.setRouting(resultSet.getString("Routing"));
//				  pspBooking.setServerIP(resultSet.getString("ServerIP"));
//				  pspBooking.setSessionId(resultSet.getString("SessionID"));
//				  pspBooking.setSiteCountry(resultSet.getString("Site_Cntry"));
//				  pspBooking.setSiteLang(resultSet.getString("Site_Lang"));
//				  pspBooking.setSkywardsID(resultSet.getString("SkywardsID"));
//				  pspBooking.setSkywardsTier(resultSet.getString("SkywardsTier"));
//				  pspBooking.setSource(resultSet.getString("Source"));
				  pspBooking.setTeenagerFare(resultSet.getString("TeenagerFare")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TeenagerFare")));
//				  pspBooking.setTeenagerTax(resultSet.getBigDecimal("TeenagerTax"));
//				  pspBooking.setTeenagerTotal(resultSet.getBigDecimal("TeenagerTotal"));
				  pspBooking.setTicketedDateGMT(resultSet.getDate("TicketeddateGMT"));
				  pspBooking.setTotal(resultSet.getString("Total")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("Total")));
				  pspBooking.setTotalPax(resultSet.getInt("TotalPax"));
//				  pspBooking.setTotalTax(resultSet.getBigDecimal("TotalTax"));
				  pspBooking.setTransactionID(resultSet.getString("TransactionID"));
				  pspBooking.setTransStatus(resultSet.getString("TransStatus"));
				  pspBooking.setTransDate(resultSet.getDate("TransDate"));
				  pspBookingsList.add(pspBooking);		  
			  }
			
			  
			} catch (SQLException e) {
				DashLoggerUtil.log(Level.ERROR, "pullDataForTheLastYear data pull failed sql exception" + e.getLocalizedMessage());
				e.printStackTrace();
			}finally{
				DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
			}
			
			
			return pspBookingsList;
			

	}
	
	*/
/*
		//pull from 00:00 today for TODAY overlay only in current scope
		public static List<SeatSellSSR> pullSeatSellData(int baseOrDelta, Long lastLoadTime) {
			
			Date javaDateForLogging = null;
			java.sql.Date sqlDate = null;
			java.sql.Date sqlEndDateBase = null;
			java.sql.Date sqlEndDateDelta = null;
			//for Oracle
			java.sql.Timestamp sqlTimestampInitial = null;
			java.sql.Timestamp sqlTimestampEndBase = null;
			java.sql.Timestamp sqlTimestampEndDelta = null;
			
			
			
			if(baseOrDelta==0){ //base
				if(DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")||DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)==null) {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(System.currentTimeMillis());
			//		cal.add(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND,0);
					cal.set(Calendar.MILLISECOND, 0);
					
					Date todayStart = cal.getTime();
					long t = todayStart.getTime();
					sqlTimestampInitial = new java.sql.Timestamp(t);
					DashLoggerUtil.log(Level.INFO, "pulling SeatSellSSR BASE data after time :::"+todayStart.toString());
					sqlDate = new java.sql.Date(todayStart.getTime());
				}else {
					String temp = DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY);
					SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
					Date date = null;
					Date start = null;
				    try {
						date = parser.parse(temp);
					} catch (ParseException e) {
						DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
						e.printStackTrace();
					}
				    Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND,0);
					cal.set(Calendar.MILLISECOND, 0);
					start = cal.getTime();
					long t = start.getTime();
					sqlTimestampInitial = new java.sql.Timestamp(t);
				    sqlDate = new java.sql.Date(start.getTime());//start date of date based overlay
				    
				    Calendar cal2 = Calendar.getInstance();
				    Date dateToday = new Date();//current time today
				    cal2.setTime(dateToday);
				    
				    Calendar cal3 = Calendar.getInstance();
				    cal3.setTime(date);//15 nov
					cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
					cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
					cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
					cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
					Date endDateBase = cal3.getTime();
					long t2 = endDateBase.getTime();
					sqlTimestampEndBase = new java.sql.Timestamp(t2);
					sqlEndDateBase = new java.sql.Date(endDateBase.getTime());

				}
			}else{
				
				if(DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")||DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)==null) {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(lastLoadTime);
					Date deltaDate = cal.getTime();
					long t = deltaDate.getTime();
					sqlTimestampInitial = new java.sql.Timestamp(t);
					DashLoggerUtil.log(Level.INFO, "pulling SeatSellSSR data after time :::"+deltaDate.toString());
					sqlDate = new java.sql.Date(deltaDate.getTime());
				}else{
					
					String temp = DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY);
					SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
					Date date = null;
				    try {
						date = parser.parse(temp);
					} catch (ParseException e) {
						DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
						e.printStackTrace();
					}
				
					
				    Calendar cal2 = Calendar.getInstance();
				    Date dateToday = new Date();//current time today
				    cal2.setTime(dateToday);
				    
				    Calendar cal3 = Calendar.getInstance();
				    cal3.setTime(date);//15 nov
					cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
					cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
					cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
					cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
					Date startDateDelta = cal3.getTime();
					long t = startDateDelta.getTime();
					sqlTimestampInitial = new java.sql.Timestamp(t);
					sqlDate = new java.sql.Date(startDateDelta.getTime());
					
					cal3.add(Calendar.MINUTE,1);
					Date endDateDelta = cal3.getTime();
					long t2 = endDateDelta.getTime();
					sqlTimestampEndDelta = new java.sql.Timestamp(t2);
					sqlEndDateDelta = new java.sql.Date(endDateDelta.getTime());
					
				}
				
			}
			
			
			Connection connection = null;
			CallableStatement callableStatement = null;
			ResultSet resultSet = null;
			SeatSellSSR seatSellSSR = null;
			List<SeatSellSSR> seatSellSSRs = new ArrayList<SeatSellSSR>();
			connection = DBConnectionPool.getConnection();
			try {
			  
			  javaDateForLogging = new Date(sqlTimestampInitial.getTime());
			  DashLoggerUtil.log(Level.INFO, "pulling SSRSeatSell data after time :::"+javaDateForLogging.toString());
			  callableStatement = connection.prepareCall(SEATSELL_TABLE_RETRIEVE);		  
			  callableStatement.setTimestamp(1, sqlTimestampInitial);
			  callableStatement.setTimestamp(2, sqlTimestampInitial);
			  if(!DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")&& DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)!=null) {
				  if(baseOrDelta==0){
					  javaDateForLogging = new Date(sqlTimestampEndBase.getTime());
					  DashLoggerUtil.log(Level.INFO, "pulling SSRSeatSell Base data till time :::"+javaDateForLogging.toString());
					  callableStatement.setTimestamp(3, sqlTimestampEndBase);//base
					  callableStatement.setTimestamp(4, sqlTimestampEndBase);//base
				  }else {
					  javaDateForLogging = new Date(sqlTimestampEndDelta.getTime());
					  DashLoggerUtil.log(Level.INFO, "pulling SSRSeatSell Delta data till time :::"+javaDateForLogging.toString());
					  callableStatement.setTimestamp(3, sqlTimestampEndDelta);//delta
					  callableStatement.setTimestamp(4, sqlTimestampEndDelta);//delta
				  }
			  }
			  
			  
			  resultSet = callableStatement.executeQuery();
			  while(resultSet.next()){

				  seatSellSSR = new SeatSellSSR();
				  seatSellSSR.setCabinClass(resultSet.getString("CabinClass"));
				  seatSellSSR.setSeatCharacteristics(resultSet.getString("seatCharacteristic"));
				  seatSellSSR.setBaseFare(resultSet.getString("base_fare"));
				  seatSellSSR.setExchangedFare(resultSet.getString("exchangedFare"));
				  seatSellSSR.setFareBrand(resultSet.getString("fareBrand"));
				  seatSellSSR.setCurrencyCode(resultSet.getString("CurrencyCode"));
				  seatSellSSR.setAdultFare(resultSet.getString("ADULTFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("ADULTFARE")));
				  seatSellSSR.setChildFare(resultSet.getString("CHILDFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("CHILDFARE")));
				  seatSellSSR.setTeenagerFare(resultSet.getString("TEENAGERFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TEENAGERFARE")));
				  seatSellSSR.setOfwFare(resultSet.getString("OFWFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("OFWFARE")));
				  seatSellSSR.setTotalPax(resultSet.getInt("TOTALPAX"));
				  
				  seatSellSSRs.add(seatSellSSR);
			  }
		  
			} catch (SQLException e) {
				DashLoggerUtil.log(Level.ERROR, "SeatSellSSR data pull failed sql exception" + e.getLocalizedMessage());
				e.printStackTrace();
			}finally{
				DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
			}
			
			
			return seatSellSSRs;
			
		}
		
*/	
	
		//
		public static List<SSRAndPaidSeatData> pullNewCombinedDataForComparison(Boolean fullDay){
			
			Date javaDateForLogging = null;
			java.sql.Date sqlDate = null;
			java.sql.Date sqlEndDate = null;
			
			//for Oracle
			java.sql.Timestamp sqlTimestampInitial = null;
			java.sql.Timestamp sqlTimestampEnd = null;
			
			//Do Yesterday
			if(DashProperties.getProperty(DashConstants.COMPARISON_DATE).equalsIgnoreCase("")||DashProperties.getProperty(DashConstants.COMPARISON_DATE)==null) {
			
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
		//		cal.add(Calendar.DAY_OF_MONTH, 1);

				cal.add(Calendar.DATE, -1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND,0);
				cal.set(Calendar.MILLISECOND, 0);
				
				Date yesterdayStart = cal.getTime();
				long t = yesterdayStart.getTime();
				sqlTimestampInitial = new java.sql.Timestamp(t);
//				DashLoggerUtil.log(Level.INFO, "pulling combined SSR and Paid seat BASE data after time :::"+todayStart.toString());
				sqlDate = new java.sql.Date(yesterdayStart.getTime());
				
				
			    Calendar cal2 = Calendar.getInstance();
			    Date dateToday = new Date();//current time today
			    cal2.setTime(dateToday);
			    
			    Calendar cal3 = Calendar.getInstance();
			    cal3.add(Calendar.DATE, -1);
//			    cal3.setTime(date);//15 nov
			    if(!fullDay) {
				cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
				cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
				cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
				cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
			    }else {
					cal3.set(Calendar.HOUR_OF_DAY, 24);
					cal3.set(Calendar.MINUTE, 0);
					cal3.set(Calendar.SECOND,0);
					cal3.set(Calendar.MILLISECOND, 0);	
			    	
			    }
				Date endDateYesterday = cal3.getTime();
				long t2 = endDateYesterday.getTime();
				sqlTimestampEnd = new java.sql.Timestamp(t2);
				sqlEndDate = new java.sql.Date(endDateYesterday.getTime());
				
			
			//get data of Comparison Date
			}else {
				
				
				String temp = DashProperties.getProperty(DashConstants.COMPARISON_DATE);
				SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
				Date date = null;
				Date start = null;
			    try {
					date = parser.parse(temp);
				} catch (ParseException e) {
					DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
					e.printStackTrace();
				}
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND,0);
				cal.set(Calendar.MILLISECOND, 0);
				start = cal.getTime();
				long t = start.getTime();
				sqlTimestampInitial = new java.sql.Timestamp(t);
			    sqlDate = new java.sql.Date(start.getTime());//start date of comparison date
				
			    Calendar cal2 = Calendar.getInstance();
			    Date dateToday = new Date();//current time today
			    cal2.setTime(dateToday);
			    
			    Calendar cal3 = Calendar.getInstance();
			    cal3.setTime(date);//15 nov
			    if(!fullDay) {
				cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
				cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
				cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
				cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
			    }else {
					cal3.set(Calendar.HOUR_OF_DAY, 24);
					cal3.set(Calendar.MINUTE, 0);
					cal3.set(Calendar.SECOND,0);
					cal3.set(Calendar.MILLISECOND, 0);	
			    }
				Date endDateOfComparison = cal3.getTime();
				long t2 = endDateOfComparison.getTime();
				sqlTimestampEnd = new java.sql.Timestamp(t2);
				sqlEndDate = new java.sql.Date(endDateOfComparison.getTime());
				
				
			}
			
			Connection connection = null;
			CallableStatement callableStatement = null;
			ResultSet resultSet = null;
			SSRAndPaidSeatData ssrAndPaidSeatData = null;
			List<SSRAndPaidSeatData> combinedDataList = new ArrayList<SSRAndPaidSeatData>();
			connection = DBConnectionPool.getConnection();
			try {
			  
				
			  javaDateForLogging = new Date(sqlTimestampInitial.getTime());
			  DashLoggerUtil.log(Level.INFO, "pulling Combined SSRSeatSell data For Comparison after time :::"+javaDateForLogging.toString());
			  callableStatement = connection.prepareCall(TABLE_RETRIEVE_NEW_DATEBASED);	
     		  callableStatement.setTimestamp(1, sqlTimestampInitial);
			  //callableStatement.setTimestamp(2, sqlTimestampInitial);
				  
			  javaDateForLogging = new Date(sqlTimestampEnd.getTime());
			  DashLoggerUtil.log(Level.INFO, "pulling Combined SSR and PaidSeat  Delta data till time :::"+javaDateForLogging.toString());
//			  callableStatement.setTimestamp(3, sqlTimestampEnd);
//			  callableStatement.setTimestamp(4, sqlTimestampEnd);
//			  callableStatement.setTimestamp(5, sqlTimestampInitial);
//			  callableStatement.setTimestamp(6, sqlTimestampEnd);
//			  callableStatement.setString(7, "0");
			  
			  callableStatement.setTimestamp(2, sqlTimestampEnd);
			
			  Calendar time = Calendar.getInstance();
			  resultSet = callableStatement.executeQuery();
			  while(resultSet.next()){
				  

		      ssrAndPaidSeatData = new SSRAndPaidSeatData();
			  ssrAndPaidSeatData.setCabinClass(resultSet.getString("CabinClass"));
			  ssrAndPaidSeatData.setSeatCharacteristics(resultSet.getString("seatCharacteristic"));
			  ssrAndPaidSeatData.setBaseFare(resultSet.getString("base_fare"));
			  ssrAndPaidSeatData.setExchangedFare(resultSet.getString("exchangedFare"));
			  ssrAndPaidSeatData.setFareBrand(resultSet.getString("fareBrand"));
			  ssrAndPaidSeatData.setCurrencyCode(resultSet.getString("CurrencyCode"));
			  ssrAndPaidSeatData.setAdultFare(resultSet.getString("ADULTFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("ADULTFARE")));
			  ssrAndPaidSeatData.setChildFare(resultSet.getString("CHILDFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("CHILDFARE")));
			  ssrAndPaidSeatData.setTeenagerFare(resultSet.getString("TEENAGERFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TEENAGERFARE")));
			  ssrAndPaidSeatData.setOfwFare(resultSet.getString("OFWFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("OFWFARE")));
			  ssrAndPaidSeatData.setTotalPax(resultSet.getInt("TOTALPAX"));
			  ssrAndPaidSeatData.setPnr(resultSet.getString("PNR"));
			  time.setTimeInMillis(resultSet.getTimestamp("TRANSDATE").getTime());
			  ssrAndPaidSeatData.setTransDate(time.getTime());
			  ssrAndPaidSeatData.setChannel(resultSet.getString("CHANNEL"));
			  ssrAndPaidSeatData.setPosCntry(resultSet.getString("POS_CNTRY"));
			  ssrAndPaidSeatData.setOrigin(resultSet.getString("ORIGIN"));
			  ssrAndPaidSeatData.setDestination(resultSet.getString("DESTINATION"));
			  time.setTimeInMillis(resultSet.getTimestamp("DEPARTUREDATE").getTime());
			  ssrAndPaidSeatData.setDepDate(time.getTime());
			  ssrAndPaidSeatData.setTotal(resultSet.getString("TOTAL")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TOTAL")));
			  ssrAndPaidSeatData.setTotalTax(resultSet.getString("TOTALTAX")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TOTALTAX")));
			  ssrAndPaidSeatData.setTax(resultSet.getString("TAX")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TAX")));
			  ssrAndPaidSeatData.setSkywardsID(resultSet.getString("SKYWARDSID")==null || "".equalsIgnoreCase(resultSet.getString("SKYWARDSID"))?"non member":"member");
			  
			  combinedDataList.add(ssrAndPaidSeatData);
			}
			  
		  
			} catch (SQLException e) {
				DashLoggerUtil.log(Level.ERROR, "SeatSellSSR data pull failed sql exception" + e.getLocalizedMessage());
				e.printStackTrace();
			}finally{
				DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
			}
			
			
			return combinedDataList;
			
		}
		
		public static PSPBookingContainer pullNewCombinedData(int baseOrDelta, Long lastLoadTime, boolean yearly ) { //,Integer lastId
					
					
					Date javaDateForLogging = null;
					Date javaDateForLogging2 = null;
					Integer lastLoadedId = null;
					java.sql.Date sqlDate = null;
					java.sql.Date sqlEndDateBase = null;
					java.sql.Date sqlEndDateDelta = null;
					java.sql.Date sqlEndDateYearly = null;
					//for Oracle
					java.sql.Timestamp sqlTimestampInitial = null;
					java.sql.Timestamp sqlTimestampEndBase = null;
					java.sql.Timestamp sqlTimestampEndDelta = null;
					java.sql.Timestamp sqlTimestampEndYearly = null;
					Long newLoadTime = null;
					Long timeToLoadNext = null;
					
					
					
					if(baseOrDelta==0 && !yearly){ //base
						if(DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")||DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)==null) {
							Calendar cal = Calendar.getInstance();
							cal.setTimeInMillis(System.currentTimeMillis());
					//		cal.add(Calendar.DAY_OF_MONTH, 1);
							cal.set(Calendar.HOUR_OF_DAY, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND,0);
							cal.set(Calendar.MILLISECOND, 0);
							
							Date todayStart = cal.getTime();
							long t = todayStart.getTime();
							sqlTimestampInitial = new java.sql.Timestamp(t);
//							DashLoggerUtil.log(Level.INFO, "pulling combined SSR and Paid seat BASE data after time :::"+todayStart.toString());
							sqlDate = new java.sql.Date(todayStart.getTime());
						}else {
							String temp = DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY);
							SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
							Date date = null;
							Date start = null;
						    try {
								date = parser.parse(temp);
							} catch (ParseException e) {
								DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
								e.printStackTrace();
							}
						    Calendar cal = Calendar.getInstance();
						    cal.setTime(date);
							cal.set(Calendar.HOUR_OF_DAY, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND,0);
							cal.set(Calendar.MILLISECOND, 0);
							start = cal.getTime();
							long t = start.getTime();
							sqlTimestampInitial = new java.sql.Timestamp(t);
						    sqlDate = new java.sql.Date(start.getTime());//start date of date based overlay
						    
						    Calendar cal2 = Calendar.getInstance();
						    Date dateToday = new Date();//current time today
						    cal2.setTime(dateToday);
						    
						    Calendar cal3 = Calendar.getInstance();
						    cal3.setTime(date);//15 nov
							cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
							cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
							cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
							cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
							Date endDateBase = cal3.getTime();
							long t2 = endDateBase.getTime();
							sqlTimestampEndBase = new java.sql.Timestamp(t2);
							sqlEndDateBase = new java.sql.Date(endDateBase.getTime());

						}
					}else if(!yearly){
						
						if(DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")||DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)==null) {
							Calendar cal = Calendar.getInstance();
							cal.setTimeInMillis(lastLoadTime);
							Date deltaDate = cal.getTime();
							long t = deltaDate.getTime();
							sqlTimestampInitial = new java.sql.Timestamp(t);
//							DashLoggerUtil.log(Level.INFO, "pulling Combined SSR and PaidSeat delta data after time :::"+deltaDate.toString());
							sqlDate = new java.sql.Date(deltaDate.getTime());
						}else{
							
							String temp = DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY);
							SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
							Date date = null;
						    try {
								date = parser.parse(temp);
							} catch (ParseException e) {
								DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
								e.printStackTrace();
							}
						
							
						    Calendar cal2 = Calendar.getInstance();
						    Date dateToday = new Date();//current time today
						    cal2.setTime(dateToday);
						    
						    Calendar cal3 = Calendar.getInstance();
						    cal3.setTime(date);//15 nov
							cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
							cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
							cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
							cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
							Date startDateDelta = cal3.getTime();
							long t = startDateDelta.getTime();
							sqlTimestampInitial = new java.sql.Timestamp(t);
							sqlDate = new java.sql.Date(startDateDelta.getTime());
							
							cal3.add(Calendar.MINUTE,1);
							Date endDateDelta = cal3.getTime();
							long t2 = endDateDelta.getTime();
							sqlTimestampEndDelta = new java.sql.Timestamp(t2);
							sqlEndDateDelta = new java.sql.Date(endDateDelta.getTime());
							
						}
						
					}else if(yearly) {
						
						Calendar calYear = Calendar.getInstance();
						calYear.setTimeInMillis(System.currentTimeMillis());
						calYear.add(Calendar.MONTH, -12);
						calYear.set(Calendar.HOUR_OF_DAY, 0);
						calYear.set(Calendar.MINUTE, 0);
						calYear.set(Calendar.SECOND,0);
						calYear.set(Calendar.MILLISECOND, 0);
						
						Date lastYearStart = calYear.getTime();
						long t = lastYearStart.getTime();
						sqlTimestampInitial = new java.sql.Timestamp(t);
						sqlDate = new java.sql.Date(lastYearStart.getTime());
//						DashLoggerUtil.log(Level.INFO, "pulling Combined SSR and PaidSeat delta data after time :::"+lastYearStart.toString());
						
						
						if(!DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("") && DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)!=null) {
							String temp = DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY);
							SimpleDateFormat parser = new SimpleDateFormat("ddMMMyyyy");
							Date date = null;
						    try {
								date = parser.parse(temp);
							} catch (ParseException e) {
								DashLoggerUtil.log(Level.ERROR,"Date Parsing error in SQLDataAccess");
								e.printStackTrace();
							}
//						    Calendar cal4 = Calendar.getInstance();
//						    cal4.setTime(date);//start of Nov15
//						    cal.set(Calendar.HOUR_OF_DAY, 0);
//							cal.set(Calendar.MINUTE, 0);
//							cal.set(Calendar.SECOND,0);
//							cal.set(Calendar.MILLISECOND, 0);
//							Date YearDataStart
						    
						
//						    sqlDate = new java.sql.Date(lastYearStart.getTime());
						    
						    Calendar cal2 = Calendar.getInstance();
						    Date dateToday = new Date();//current time today
						    cal2.setTime(dateToday);
						    
						    Calendar cal3 = Calendar.getInstance();
						    cal3.setTime(date);//15 nov
							cal3.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
							cal3.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
							cal3.set(Calendar.SECOND,cal2.get(Calendar.SECOND));
							cal3.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
							Date endDateHistorical = cal3.getTime();
							long t2 = endDateHistorical.getTime();
							sqlTimestampEndYearly = new java.sql.Timestamp(t2);
							sqlEndDateYearly = new java.sql.Date(endDateHistorical.getTime());
							
							
						}else {//yearly Data for this year without DATE BASED OVERLAY
					
						}
						
						
					}
					
					
					Connection connection = null;
					CallableStatement callableStatement = null;
					CallableStatement callableStatementTest = null;
//					PreparedStatement prepStatement = null;
					ResultSet resultSet = null;
					ResultSet resultSetTest = null;
					SSRAndPaidSeatData ssrAndPaidSeatData = null;
					List<SSRAndPaidSeatData> combinedDataList = new ArrayList<SSRAndPaidSeatData>();
					connection = DBConnectionPool.getConnection();
					try {
					  
					  
					  if(!DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")&& DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)!=null) {
						  callableStatement = connection.prepareCall(TABLE_RETRIEVE_NEW_DATEBASED);	
					  }else {
						  callableStatement = connection.prepareCall(TABLE_RETRIEVE_NEW_LIVE);

					  }

					  if(!DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY).equalsIgnoreCase("")&& DashProperties.getProperty(DashConstants.DATE_BASED_OVERLAY)!=null) {
						  
						  callableStatement.setTimestamp(1, sqlTimestampInitial);
						  /*
						  callableStatement.setDate(1, sqlDate);
						  
						  callableStatement.setTimestamp(2, sqlTimestampInitial);
						  callableStatement.setTimestamp(5, sqlTimestampInitial);//Initial for base
						  */
						  if(baseOrDelta==0){



							  if(yearly) {
								  javaDateForLogging = new Date(sqlTimestampEndYearly.getTime());

								  /*
								  callableStatement.setDate(3, sqlEndDateYearly);//base year
								  callableStatement.setTimestamp(4, sqlTimestampEndYearly);//base year
								  callableStatement.setTimestamp(6, sqlTimestampEndYearly);//base year end
								  */
								  callableStatement.setTimestamp(2, sqlTimestampEndYearly);//base year
								  DashLoggerUtil.log(Level.INFO, "pulling Combined SSR and PaidSeat Base data till time :::"+javaDateForLogging.toString());
							  }else {
								  javaDateForLogging = new Date(sqlTimestampEndBase.getTime());
								  DashLoggerUtil.log(Level.INFO, "pulling Combined SSR and PaidSeat Base data till time :::"+javaDateForLogging.toString());
 
								  
								  /*
								  callableStatement.setDate(3, sqlEndDateBase);//base
								  callableStatement.setTimestamp(4, sqlTimestampEndBase);//base
								  callableStatement.setTimestamp(6, sqlTimestampEndBase);//base end	
								  */
								  callableStatement.setTimestamp(2, sqlTimestampEndBase);//base end
							  }

							  

						  }else {
							  javaDateForLogging = new Date(sqlTimestampEndDelta.getTime());
							  DashLoggerUtil.log(Level.INFO, "pulling Combined SSR and PaidSeat  Delta data till time :::"+javaDateForLogging.toString());

							  /*
							  callableStatement.setDate(3, sqlEndDateDelta);//delta
							  callableStatement.setTimestamp(4, sqlTimestampEndDelta);//delta
							  callableStatement.setTimestamp(6, sqlTimestampEndDelta);//delta end
							  */
							  
							  callableStatement.setTimestamp(2, sqlTimestampEndDelta);//delta end
							  
							  
						  }
//						  callableStatement.setInt(7, lastId);
					  }else {
						  						 
						  /*
						  callableStatement.setDate(1, sqlDate);
						  callableStatement.setTimestamp(2, sqlTimestampInitial);
						  callableStatement.setTimestamp(3, sqlTimestampInitial);
						  
						  */
						  callableStatement.setTimestamp(1, sqlTimestampInitial);
//						  callableStatement.setInt(4, lastId);

					  }
					  
					  
					  javaDateForLogging = new Date(sqlTimestampInitial.getTime());
					  javaDateForLogging2 = new Date(System.currentTimeMillis());
					  DashLoggerUtil.log(Level.INFO, "pulling Combined SSRSeatSell data after time :::"+javaDateForLogging.toString() + "::pulling at system time::"+javaDateForLogging2);
					  
					  Calendar time = Calendar.getInstance();
					  newLoadTime = System.currentTimeMillis();
					  resultSet = callableStatement.executeQuery();
					  
					  
                      
                      
					  while(resultSet.next()){


						  ssrAndPaidSeatData = new SSRAndPaidSeatData();
						  ssrAndPaidSeatData.setCabinClass(resultSet.getString("CabinClass"));
						  ssrAndPaidSeatData.setSeatCharacteristics(resultSet.getString("seatCharacteristic"));
						  ssrAndPaidSeatData.setBaseFare(resultSet.getString("base_fare"));
						  ssrAndPaidSeatData.setExchangedFare(resultSet.getString("exchangedFare"));
						  ssrAndPaidSeatData.setFareBrand(resultSet.getString("fareBrand"));
						  ssrAndPaidSeatData.setCurrencyCode(resultSet.getString("CurrencyCode"));
						  ssrAndPaidSeatData.setAdultFare(resultSet.getString("ADULTFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("ADULTFARE")));
						  ssrAndPaidSeatData.setChildFare(resultSet.getString("CHILDFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("CHILDFARE")));
						  ssrAndPaidSeatData.setTeenagerFare(resultSet.getString("TEENAGERFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TEENAGERFARE")));
						  ssrAndPaidSeatData.setOfwFare(resultSet.getString("OFWFARE")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("OFWFARE")));
						  ssrAndPaidSeatData.setTotalPax(resultSet.getInt("TOTALPAX"));
						  ssrAndPaidSeatData.setPnr(resultSet.getString("PNR"));
						  time.setTimeInMillis(resultSet.getTimestamp("TRANSDATE").getTime());
						  timeToLoadNext = time.getTimeInMillis();
						  ssrAndPaidSeatData.setTransDate(time.getTime());
						  ssrAndPaidSeatData.setChannel(resultSet.getString("CHANNEL"));
						  ssrAndPaidSeatData.setPosCntry(resultSet.getString("POS_CNTRY"));
						  ssrAndPaidSeatData.setOrigin(resultSet.getString("ORIGIN"));
						  ssrAndPaidSeatData.setDestination(resultSet.getString("DESTINATION"));
						  time.setTimeInMillis(resultSet.getTimestamp("DEPARTUREDATE").getTime());
						  ssrAndPaidSeatData.setDepDate(time.getTime());
						  ssrAndPaidSeatData.setTotal(resultSet.getString("TOTAL")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TOTAL")));
						  ssrAndPaidSeatData.setTotalTax(resultSet.getString("TOTALTAX")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TOTALTAX")));
						  ssrAndPaidSeatData.setTax(resultSet.getString("TAX")==null?new BigDecimal(0):new BigDecimal(resultSet.getString("TAX")));
//						  lastLoadedId = resultSet.getInt("ID");
						  ssrAndPaidSeatData.setSkywardsID(resultSet.getString("SKYWARDSID")==null || "".equalsIgnoreCase(resultSet.getString("SKYWARDSID"))?"non member":"member");
						  if(baseOrDelta==1)
							  DashLoggerUtil.log(Level.INFO, "pulled row PNR ::"+ ssrAndPaidSeatData.getPnr()+ "::pulled Row transDate :: " + ssrAndPaidSeatData.getTransDate());
						  combinedDataList.add(ssrAndPaidSeatData);
					  }
					  
					  javaDateForLogging2 = new Date(System.currentTimeMillis());
					  DashLoggerUtil.log(Level.INFO, "pulling Combined SSRSeatSell TEST QUERY data at system time::"+javaDateForLogging2);
					  callableStatementTest = connection.prepareCall(TEST_QUERY);
					  Calendar calTest = Calendar.getInstance();
					  calTest.setTimeInMillis(System.currentTimeMillis());
					  calTest.set(Calendar.HOUR_OF_DAY, 0);
					  calTest.set(Calendar.MINUTE, 0);
					  calTest.set(Calendar.SECOND,0);
					  calTest.set(Calendar.MILLISECOND, 0);
					  Date todayStartTest = calTest.getTime();
					  long tTest = todayStartTest.getTime();
					  java.sql.Timestamp sqlTimestampTest = new java.sql.Timestamp(tTest);
					  callableStatementTest.setTimestamp(1, sqlTimestampTest);
					  callableStatementTest.setTimestamp(2, sqlTimestampTest);
					  callableStatementTest.setTimestamp(3, sqlTimestampTest);
					  /*
					  try {
						  resultSetTest = callableStatementTest.executeQuery();
						  while(resultSetTest.next()){
					  
						  DashLoggerUtil.log(Level.INFO,resultSetTest.getString("Count"));
						  
						  }
					  }catch(Exception e) {
						e.printStackTrace();
						DashLoggerUtil.log(Level.ERROR, "Test Query Failing"+e.getCause()+e.getMessage() + e.getMessage());
					  }

					 */
					 //PSPBookingContainer.setLastLoaded(newLoadTime);
					 if(timeToLoadNext==null)
						 PSPBookingContainer.setLastLoaded(lastLoadTime);
					 else
						 PSPBookingContainer.setLastLoaded(timeToLoadNext);
					 //PSPBookingContainer.setLastLoadedId(lastLoadedId);
					 PSPBookingContainer.getInstance().setPspBookingsNew(combinedDataList);
					  
				  
					} catch (SQLException e) {
						DashLoggerUtil.log(Level.ERROR, "SeatSellSSR data pull failed sql exception" + e.getLocalizedMessage());
						e.printStackTrace();
					}finally{
						DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);
						DBConnectionPool.releaseConnection(resultSetTest, callableStatementTest, connection);
					}
					
					return PSPBookingContainer.getInstance();
					
		}
		
		
		public static HashMap<String,HaulType> pullHaulTypes() {
					
			Connection connection = null;
			CallableStatement callableStatement = null;
			ResultSet resultSet = null;
			HaulType haulType = null;
			HashMap<String,HaulType> haulTypesMap = new HashMap<String,HaulType>();
			connection = DBConnectionPool.getConnection();
			try {
	          
			  callableStatement = connection.prepareCall(HAUL_TYPES_TABLE_RETRIEVE);		  
			  resultSet = callableStatement.executeQuery();
			  while(resultSet.next()){

				  haulType = new HaulType();
				  haulType.setODCountry(resultSet.getString("OD_COUNTRY"));
				  haulType.setOrigin(resultSet.getString("ORIGIN"));
				  haulType.setDestination(resultSet.getString("DESTINATION"));
				  haulType.setHaul(resultSet.getString("HAUL"));

				  haulTypesMap.put(haulType.getODCountry(), haulType);
			  }
		  
			} catch (SQLException e) {
				DashLoggerUtil.log(Level.ERROR, "HAUL TYPES TABLE DOES NOT EXIST" + e.getLocalizedMessage());
				e.printStackTrace();
			}finally{
				DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
			}
			
			
			return haulTypesMap;
			
		}
		
		public static HashMap<String,Double> pullCurrencyRates() {
			
			Connection connection = null;
			CallableStatement callableStatement = null;
			ResultSet resultSet = null;
			HashMap<String,Double> currencyRatesMap = new HashMap<String,Double>();
			connection = DBConnectionPool.getConnection();
			try {
	          
			  callableStatement = connection.prepareCall(CURRENCY_RATES_TABLE_RETRIEVE);		  
			  resultSet = callableStatement.executeQuery();
			  while(resultSet.next()){
				  currencyRatesMap.put(resultSet.getString("CURRENCYCODE"), resultSet.getDouble("EXCHANGERATE"));
			  }
		  
			} catch (SQLException e) {
				DashLoggerUtil.log(Level.ERROR, "CURRENCY TABLE READ ERROR" + e.getLocalizedMessage());
				e.printStackTrace();
			}finally{
				DBConnectionPool.releaseConnection(resultSet, callableStatement, connection);			    	
			}
			
			
			return currencyRatesMap;
			
		}
		
		
}
