package com.emirates.dash.engine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;

import com.emirates.dash.dao.LocationXMLReader;
import com.emirates.dash.dao.SQLDataAccess;
import com.emirates.dash.dto.MasterResponseDTO;
import com.emirates.dash.model.PSPBooking;
import com.emirates.dash.model.PSPBookingContainer;
import com.emirates.dash.model.SSRAndPaidSeatData;
import com.emirates.dash.model.SeatSellSSR;
import com.emirates.dash.utilities.DBDataAccessScheduler;
import com.emirates.dash.utilities.DashCacheUtil;
import com.emirates.dash.utilities.DashConstants;
import com.emirates.dash.utilities.DashLoggerUtil;
import com.emirates.dash.utilities.DashProperties;
import com.emirates.dash.utilities.Mode;
import com.emirates.dash.utilities.Overlay;
import com.emirates.dash.vo.CacheVO;
import com.emirates.dash.vo.CacheVOContainer;
import com.emirates.dash.vo.ModeTimeStampVO;
import com.emirates.dash.vo.MonthlyVO;

public class DataFlowOrchestrator {

	private static DataFlowOrchestrator OrchestratorObject;
	//set up a flag to feed dummy data into the Cache
	private static boolean mockData;
	
	protected DataFlowOrchestrator(){
		
	}

	public static synchronized DataFlowOrchestrator getInstance() {
		if (OrchestratorObject == null) {
			OrchestratorObject = new DataFlowOrchestrator();
			mockData = DashProperties.getPropertyAsBoolean("mockData","false");
		} 
			return OrchestratorObject;
	}

    public void initiateReadCycle(ModeTimeStampVO modeTimeStampVO){
		
    	DashLoggerUtil.log(Level.INFO, "Mineutely Read cycle initiated");
//    	List<PSPBooking> pspBaseBookingsForOverlay = null;  
//    	List<PSPBooking> pspBookings = null;
//    	List<SeatSellSSR> seatSellSSRsForOverlay = null;  
//    	List<SeatSellSSR> seatSellSSRs = null;
        List<Long> delaysPSP = null;
        CacheVOContainer cacheVOContainer = null;
//        ArrayList<MonthlyVO> monthlyBreakdown = null;
    	//CHANGED
        List<SSRAndPaidSeatData> combinedDataList = null;
    	List<SSRAndPaidSeatData> combinedDataListForOverlay = null;
    	List<SSRAndPaidSeatData> combinedDataListForComparison = null;
    	
    	try{
//    	if(mockData){    
//    		//pspBaseBookings = SQLDataAccess.pullPSPMockBaseData(); BASE DATA TO BE SOURCED FROM CACHE
//    		pspBookings = SQLDataAccess.pullPSPMockData();//this the delta data only 
//    		seatSellSSRs = SQLDataAccess.pullSeatSellMockData(1);//delta
//    		
//    	}else{
    		//pspBaseBookings = SQLDataAccess.pullPSPLiveBaseData(); BASE DATA TO BE SOURCED FROM CACHE
    		//CHANGED
    	PSPBookingContainer pspbc = SQLDataAccess.pullNewCombinedData(1, modeTimeStampVO.getModeTime().get(Mode.PAIDSEAT + "-" +Overlay.TODAY), false);//,modeTimeStampVO.getLastLoadedId()
    	combinedDataList = pspbc.getPspBookingsNew();
    	DashLoggerUtil.log(Level.INFO, "Minutely Data pull resultset size ::::" + combinedDataList.size());

    	combinedDataListForComparison = (List<SSRAndPaidSeatData>)DashCacheUtil.getDataFromCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.COMPARISON);
    	DashLoggerUtil.log(Level.INFO, "Overlay Raw size -1 for Comparison::"+ combinedDataListForComparison.size());
		//PSPBookingContainer.setLastLoaded(System.currentTimeMillis());
		
		//only PAIDSEAT and TODAY in scope
		//CHANGED
		combinedDataListForOverlay = (List<SSRAndPaidSeatData>)DashCacheUtil.getDataFromCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW);
		DashLoggerUtil.log(Level.INFO, "Overlay Raw size 1::"+ combinedDataListForOverlay.size());
//		pspBaseBookingsForOverlay = (List<PSPBooking>)DashCacheUtil.getDataFromCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW);
//		seatSellSSRsForOverlay = (List<SeatSellSSR>)DashCacheUtil.getDataFromCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW_SEATSELL);
//		monthlyBreakdown = (ArrayList<MonthlyVO>) DashCacheUtil.getDataFromCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.FOR_THE_YEAR);
		
		
		/*
		RevenueBookingContainer rbc = RevenueBookingContainer.getInstance();
		if(mockData){
		    rbc.setRevenueBookings(SQLDataAccess.pullBookingData(pspbc.getPspBookings().get(pspbc.getPspBookings().size()-1).getTransDate()));
		    RevenueBookingContainer.setLastLoaded(System.currentTimeMillis());
		}else{
			//pull from yesterday early monring 00:00
			//rbc.setRevenueBookings(SQLDataAccess.pullBookingData());
		}

        */
		
		if(combinedDataList!=null && combinedDataList.size()>0) {	
			for(Mode thisMode : Mode.values()){
				for(Overlay thisOverlay : Overlay.values()){
					if(thisMode.equals(Mode.PAIDSEAT) && thisOverlay.equals(Overlay.TODAY)){
					  //the below cacheVOContainer will replace the existing one
					  cacheVOContainer = new CacheVOContainer();
					  switch(thisMode){
					  	//CHANGED ALL
					  	case PAIDSEAT : //These delays will only accurately correspond to [PaidSeat_Booking_Details] sourced data tables, SeatSellSSR sourced data will be artificially populated
					  		            //within the same delays and any extra data that needs to be pushed will be pushed in at the last delay.
					  		            delaysPSP = calculateDelays(pspbc, cacheVOContainer, thisMode, thisOverlay);
					  	
					  	                //BusinessTransformsForPull.getInstance().calculateTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay, mockData, delaysPSP, pspBaseBookingsForOverlay);
					  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay,DashConstants.ALL, combinedDataListForComparison);
					  	                //DONOT have to reget raw data for calculationg of next table
					  	                //pspBaseBookingsForOverlay = (List<PSPBooking>)DashCacheUtil.getDataFromCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW);
					  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay,DashConstants.WEB,combinedDataListForComparison);
					  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay,DashConstants.MOB,combinedDataListForComparison);
					  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay,DashConstants.MAND,combinedDataListForComparison);
					  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay,DashConstants.MIPH,combinedDataListForComparison);
					  	                
					  	                BusinessTransformsForPull.getInstance().initializeChannelStats(pspbc,cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay);
					  	                BusinessTransformsForPull.getInstance().initializeOverallStats(pspbc,cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay, combinedDataListForComparison);				  	            
					  	                BusinessTransformsForPull.getInstance().initializeSeatCharacteristics(pspbc, cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay);
					  	                BusinessTransformsForPull.getInstance().initializeBrandSplit(pspbc, cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay);
//					  	                monthlyBreakdown = BusinessTransformsForPull.getInstance().initializeMonthlyBreakdown(pspbc,cacheVOContainer, thisMode, thisOverlay, monthlyBreakdown);
					  	                //setting into cache might not be needed because objects passed by reference
//					  	                DashCacheUtil.setDataInCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.FOR_THE_YEAR, monthlyBreakdown, PSPBookingContainer.getLastLoaded());
					  	                BusinessTransformsForPull.getInstance().initializeHaulSplit(pspbc,cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay);
					  	                BusinessTransformsForPull.getInstance().initializeMonthlyBreakdownNew(pspbc,cacheVOContainer, thisMode, thisOverlay,combinedDataListForOverlay);
					  	                BusinessTransformsForPull.getInstance().initializeSkywardsData(pspbc,cacheVOContainer, thisMode, thisOverlay, combinedDataListForOverlay);
					  	                cleanCacheVOContainer(cacheVOContainer, delaysPSP);//POS activity to be populated only after clean cacheContainer 
					  	                BusinessTransformsForPull.getInstance().populatePOSActivityForMap(pspbc, cacheVOContainer, thisMode, thisOverlay,  delaysPSP);
					
					  	default: break;
					  } 
					  
					  DashCacheUtil.cleanCache(thisMode +"-" + thisOverlay);
					  DashCacheUtil.setDataInCache(thisMode +"-" + thisOverlay, cacheVOContainer, PSPBookingContainer.getLastLoaded());
					  ModeTimeStampVO.setLastLoadedId( PSPBookingContainer.getLastLoadedId());
					  
				   }
				}
			}
		}else {//flushing POS activity/extra cache images/delays and resetting lastLoaded
			for(Mode thisMode : Mode.values()){
				for(Overlay thisOverlay : Overlay.values()){
					if(thisMode.equals(Mode.PAIDSEAT) && thisOverlay.equals(Overlay.TODAY)){
						
						cacheVOContainer = (CacheVOContainer)DashCacheUtil.getDataFromCache(thisMode +"-" + thisOverlay);
						for(int i =0; i<cacheVOContainer.getCacheImages().size();i++) {
							if(i==cacheVOContainer.getCacheImages().size()-1) {
								cacheVOContainer.getCacheImages().get(i).setPosActivity(null);
								cacheVOContainer.getCacheImages().get(i).setDelay(0);
							}else {
								
								cacheVOContainer.getCacheImages().remove(i);
							}
						}
						DashCacheUtil.cleanCache(thisMode +"-" + thisOverlay);
						DashCacheUtil.setDataInCache(thisMode +"-" + thisOverlay, cacheVOContainer, PSPBookingContainer.getLastLoaded());
					}
				}
			}	
			 
		}
		}catch(Exception e){
			DashLoggerUtil.log(Level.ERROR, "Data flow Engine error - see stack trace");
			e.printStackTrace();			
		}
		
		//CHANGED
//		pspBaseBookingsForOverlay.addAll(pspBookings);
//		seatSellSSRsForOverlay.addAll(seatSellSSRs);
		combinedDataListForOverlay.addAll(combinedDataList);
		DashCacheUtil.cleanCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW);
		DashCacheUtil.setDataInCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW, combinedDataListForOverlay, PSPBookingContainer.getLastLoaded());
		DashLoggerUtil.log(Level.INFO, "Overlay Raw size 2::"+ combinedDataListForOverlay.size());
//		DashCacheUtil.setDataInCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW_SEATSELL, seatSellSSRsForOverlay, PSPBookingContainer.getLastLoaded());
		PSPBookingContainer.flushModel();
	   	
    }


    //if delays are the same, then converges cache images for that delay into 1 image
	private void cleanCacheVOContainer(CacheVOContainer cacheVOContainer, List<Long> delaysPSP) {

		int count = 0;
		for(CacheVO cacheImage : cacheVOContainer.getCacheImages()){
			
			cacheImage.setDelay(delaysPSP.get(count).intValue());		
			count++;
		}
		
		for(int i = 0;i<cacheVOContainer.getCacheImages().size();){
			
			if(i+1<cacheVOContainer.getCacheImages().size() && cacheVOContainer.getCacheImages().get(i).getDelay() == cacheVOContainer.getCacheImages().get(i+1).getDelay()){
				cacheVOContainer.getCacheImages().remove(i);
			}else{
				i++;
			}

		}

	}

	//TODO for all modes
	public void populateCachesOnStartup() {
		

//    	List<PSPBooking> pspBookings = null;
//    	List<PSPBooking> pspBookingsForLastYear = null;
    	List<SSRAndPaidSeatData> combinedDataList = null;
    	List<SSRAndPaidSeatData> combinedDataListYearly = null;
    	List<SSRAndPaidSeatData> combinedDataListForComparison = null;
        //List<Long> delaysPSP = null; //delays are to be null for now
//    	List<SeatSellSSR> seatSellSSRs = null;
//    	ArrayList<MonthlyVO> monthlyBreakDown = null;
        MasterResponseDTO locationsMapContainer = LocationXMLReader.retrieveLocationsJSON();
        DashCacheUtil.setDataInCache(DashConstants.LOCATIONS_CACHE_KEY, locationsMapContainer, System.currentTimeMillis());
        DashCacheUtil.setDataInCache(DashConstants.HAUL_TYPES_TABLE, SQLDataAccess.pullHaulTypes(), System.currentTimeMillis());
        DashCacheUtil.setDataInCache(DashConstants.CURRENCY_RATES_TABLE, SQLDataAccess.pullCurrencyRates(), System.currentTimeMillis());
         
        
        CacheVOContainer cacheVOContainer = null;
        
       	PSPBookingContainer pspbc  = SQLDataAccess.pullNewCombinedData(0, null, false);
       	combinedDataList = pspbc.getPspBookingsNew();
    	DashLoggerUtil.log(Level.INFO, "Todays Data size at startup"+ combinedDataList.size());

    	combinedDataListForComparison = SQLDataAccess.pullNewCombinedDataForComparison(true);
   		DashLoggerUtil.log(Level.INFO, "Comparison Data size at startup"+ combinedDataListForComparison.size());

		pspbc.setPspBookingsNew(combinedDataList);
		
//		PSPBookingContainer.setLastLoaded(System.currentTimeMillis());
		//CHANGED	
		DashCacheUtil.setDataInCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW, combinedDataList, PSPBookingContainer.getLastLoaded());
		DashCacheUtil.setDataInCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.COMPARISON, combinedDataListForComparison, System.currentTimeMillis());

		
			
		for(Mode thisMode : Mode.values()){
			for(Overlay thisOverlay : Overlay.values()){
			 if(thisOverlay.equals(Overlay.TODAY)){
				  //the below cacheVOContainer will replace the existing one
				  //for dashboard startup number of cacheimages will always be one.
				  cacheVOContainer = new CacheVOContainer();
				  switch(thisMode){
				  
				  	case PAIDSEAT : 
				  		            //TODO need to calculate delays of the last one minute
				  		            
				                    //Business transforms
				  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay ,null,DashConstants.ALL, combinedDataListForComparison);
				  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay ,null,DashConstants.WEB, combinedDataListForComparison);
				  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay ,null,DashConstants.MOB, combinedDataListForComparison);
				  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay ,null,DashConstants.MAND, combinedDataListForComparison);
				  	                BusinessTransformsForPull.getInstance().initializeTop10POS(pspbc, cacheVOContainer, thisMode, thisOverlay ,null,DashConstants.MIPH, combinedDataListForComparison);
				  	                
				  	                BusinessTransformsForPull.getInstance().initializeChannelStats(pspbc, cacheVOContainer, thisMode, thisOverlay, null);
				  	                BusinessTransformsForPull.getInstance().initializeOverallStats(pspbc, cacheVOContainer, thisMode, thisOverlay, null, combinedDataListForComparison);
				  	                BusinessTransformsForPull.getInstance().populatePOSActivityForMap(pspbc, cacheVOContainer, thisMode, thisOverlay, null);
				  	                BusinessTransformsForPull.getInstance().initializeSeatCharacteristics(pspbc, cacheVOContainer, thisMode, thisOverlay, null);
				  	                BusinessTransformsForPull.getInstance().initializeBrandSplit(pspbc, cacheVOContainer, thisMode, thisOverlay, null);
//				  	                monthlyBreakDown = BusinessTransformsForPull.getInstance().initializeMonthlyBreakdown(pspbc,cacheVOContainer, thisMode, thisOverlay, null);
				  	                BusinessTransformsForPull.getInstance().initializeHaulSplit(pspbc,cacheVOContainer, thisMode, thisOverlay, null);
				  	                BusinessTransformsForPull.getInstance().initializeMonthlyBreakdownNew(pspbc,cacheVOContainer, thisMode, thisOverlay, null);
				  	                BusinessTransformsForPull.getInstance().initializeSkywardsData(pspbc,cacheVOContainer, thisMode, thisOverlay, null);
//				  	                DashCacheUtil.setDataInCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.FOR_THE_YEAR, monthlyBreakDown, PSPBookingContainer.getLastLoaded());
				  	                
				
				  	default: break;
				  } 
				  
				  DashCacheUtil.setDataInCache(thisMode +"-" + thisOverlay, cacheVOContainer, PSPBookingContainer.getLastLoaded());
				}
			 }
		}
		
		

		PSPBookingContainer.flushModel();
		
	}
	
	//if(Mock) take the top 10 PSPBookings and set artificial delays for them
	//else create real delays for all PSPBookings after input date
	private List<Long> calculateDelays(PSPBookingContainer pspbc, CacheVOContainer cacheVOContainer, Mode thisMode, Overlay thisOverlay) {
		

		Calendar cal = null;
		Integer transactionTimeInSec = null;
		List<Long> delays = new ArrayList<Long>();

		//assuming pspbc is already sorted
		if(mockData){
			
			Long i = 10l;
			//CHANGED
//			for(PSPBooking pspBooking : pspbc.getPspBookings()){
			for(SSRAndPaidSeatData pspBooking : pspbc.getPspBookingsNew()){
				
				delays.add(i);
				if(i+10<60){
					i = i+10;
				}else{
					i = 59l;
				}
				
			}
			
			
		}else{
			
		    Integer firstTransTimeSec = 0;
		    Integer firstTransTimeMin = 0;
		    int count = 0;
		    boolean shiftDelaysbyFirstTransTime = false;
			//CHANGED
//			for(PSPBooking pspBooking : pspbc.getPspBookings()){
			for(SSRAndPaidSeatData pspBooking : pspbc.getPspBookingsNew()){
				
				cal = Calendar.getInstance();
				cal.setTime(pspBooking.getTransDate());
				transactionTimeInSec = cal.get(Calendar.SECOND);
				if(count==0){
					firstTransTimeSec = transactionTimeInSec;
					firstTransTimeMin = cal.get(Calendar.MINUTE);
				}else if(firstTransTimeSec>=transactionTimeInSec && firstTransTimeMin!=cal.get(Calendar.MINUTE)){
					shiftDelaysbyFirstTransTime = true;					
				}
     			delays.add(transactionTimeInSec.longValue());
				cal.clear();
				transactionTimeInSec = null;
				count++;
			}
			
			//spacing delays over a minute in case the queries have been scheduled to run at a different phase as compared to server time.
			if(shiftDelaysbyFirstTransTime){
				
				for(int i = 0; i<delays.size(); i++){
					
					if(delays.get(i)-firstTransTimeSec<0 && i!=0){
						delays.set(i, (60 - firstTransTimeSec + delays.get(i))>59?59:60 - firstTransTimeSec + delays.get(i));
					}else{
						delays.set(i, delays.get(i) - firstTransTimeSec );
					}
					
				}	
			}
		
		}
		
		Collections.sort(delays);
		
		for(Long delay : delays) {
			DashLoggerUtil.log(Level.DEBUG,"Delays are : " + delay.toString());
		}

		

			
		return delays;
	}

	//CHANGED
	public void initiateDataRefreshForOverlay(){
		
		System.out.println("DAILY DATA REFRESH INITIATED::::");

		DBDataAccessScheduler.pauseTimer();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<PSPBooking> pspBookings = null;

		List<SSRAndPaidSeatData> combinedDataList = null;
		List<SSRAndPaidSeatData> combinedDataListForComparison = null;
//		List<SSRAndPaidSeatData> combinedDataListYearly = null;
//		ArrayList<MonthlyVO> monthlyBreakdown = null;
		
		if(DashProperties.getProperty(DashConstants.COMPARISON_DATE).equalsIgnoreCase("")||DashProperties.getProperty(DashConstants.COMPARISON_DATE)==null) {
			combinedDataListForComparison = (List<SSRAndPaidSeatData>)DashCacheUtil.getDataFromCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW);
		}else {
			combinedDataListForComparison = SQLDataAccess.pullNewCombinedDataForComparison(true);
		}
		
		DashLoggerUtil.log(Level.INFO, "Overlay Raw size -1 for Comparison::"+ combinedDataListForComparison.size());
		DashCacheUtil.cleanCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.COMPARISON);
		DashCacheUtil.setDataInCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.COMPARISON, combinedDataListForComparison, System.currentTimeMillis());
		 
   		//CHANGED
		PSPBookingContainer pspbc = SQLDataAccess.pullNewCombinedData(0, null, false);
		combinedDataList = pspbc.getPspBookingsNew();

		//CHANGED
    	DashLoggerUtil.log(Level.INFO, "Overlay Raw size 0::"+ combinedDataList.size());
    	DashCacheUtil.cleanCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW);
		DashCacheUtil.setDataInCache(Mode.PAIDSEAT +"-" +Overlay.TODAY+DashConstants.RAW, combinedDataList, pspbc.getLastLoaded());
		//DashCacheUtil.cleanCache(DashConstants.HOURLY_BREAKDOWN_TODAY_PAIDSEAT);
		DBDataAccessScheduler.unpauseTimer();

		
		
	}
	
	
}
