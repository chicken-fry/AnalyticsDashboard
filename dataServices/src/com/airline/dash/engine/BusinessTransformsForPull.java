package com.emirates.dash.engine;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Level;

import com.emirates.dash.dao.LocationXMLReader;
import com.emirates.dash.dto.MasterResponseDTO;
import com.emirates.dash.model.HaulType;
import com.emirates.dash.model.PSPBooking;
import com.emirates.dash.model.PSPBookingContainer;
import com.emirates.dash.model.SSRAndPaidSeatData;
import com.emirates.dash.model.SeatSellSSR;
import com.emirates.dash.utilities.DashCacheUtil;
import com.emirates.dash.utilities.DashConstants;
import com.emirates.dash.utilities.DashLoggerUtil;
import com.emirates.dash.utilities.DashProperties;
import com.emirates.dash.utilities.MapSortUtil;
import com.emirates.dash.utilities.Mode;
import com.emirates.dash.utilities.Overlay;
import com.emirates.dash.vo.CacheVO;
import com.emirates.dash.vo.CacheVOContainer;
import com.emirates.dash.vo.ChannelVO;
import com.emirates.dash.vo.LatLongVO;
import com.emirates.dash.vo.MapEventVO;
import com.emirates.dash.vo.MonthlyVO;
import com.emirates.dash.vo.OverallStatsVO;
import com.emirates.dash.vo.SkywardMembers;
import com.emirates.dash.vo.Top10PosVO;
import com.emirates.dash.vo.TopBrandsVO;
import com.emirates.dash.vo.TopHaulVO;
import com.emirates.dash.vo.TopSeatCharVO;

public class BusinessTransformsForPull {
	

	//DEV NOTES - START
	/*if a booking has occured on the same POS at the 5th second and the
	55th second, data will be collated and sent to ui for display on 
	the 55th second*/
	
	/* the frontend to display event for 10 seconds, before displaying next event
	 * on the same POS
	 */

	//keep only yesterday(Configurable) 00:00 data till present in Cache
	//call this method before updating cache - so that recalculating 
	//percentages makes sense for given time period.
    //DEV NOTES - END
	
	private static BusinessTransformsForPull businessTransformObject;
//	private static CountryCodeCacheDashboard countryCodeCache;
    protected BusinessTransformsForPull(){
		
	}

	public static synchronized BusinessTransformsForPull getInstance() {
		if (businessTransformObject == null) {
			businessTransformObject = new BusinessTransformsForPull();
			
//			countryCodeCache = CountryCodeCacheDashboard.getInstance();
		} 
			return businessTransformObject;
	}
	
	

	//this will not use cache details, it will only use its arguments to initialize top 10 and populate cache.
	//for dashboard startup number of cacheimages will always be one and there will not be any delays
	//will set lastLoadedTime
	//TODO consume currency converstion service
	//CHANGED
	public void initializeTop10POS(PSPBookingContainer pspbc,
			CacheVOContainer cacheVOContainer, Mode thisMode,
			Overlay thisOverlay, List<SSRAndPaidSeatData> pspBaseBookings, String channels, List<SSRAndPaidSeatData> combinedDataListForComparison) {


		
		List<SSRAndPaidSeatData> overlayBaseBookingsForManipulation = deepCopyCombined(pspBaseBookings);
		List<SSRAndPaidSeatData> pspBookingsForManipulation = deepCopyCombined(pspbc.getPspBookingsNew());
		List<SSRAndPaidSeatData> overlayComparisonBookingsForManipulation = deepCopyCombined(combinedDataListForComparison);
		//UNUSED CURRENTLY
		//24 hours or yesterday or today
		//trimDataAsPerOverlay(thisOverlay, pspBaseBookings);//trim without including within the minute records which is pspbc
		List<SSRAndPaidSeatData> channelSpecificPSPBookings = trimBookingsAsPerChannel(pspBookingsForManipulation, overlayBaseBookingsForManipulation, overlayComparisonBookingsForManipulation, channels);
		
		if((channelSpecificPSPBookings==null || channelSpecificPSPBookings.size()==0)) {
			
			DashLoggerUtil.log(Level.DEBUG, "no bookings for this channel ::" + channels);
		}
		
		//only one image for mode-overlay in case of first invocation : 
		//CREATING OBJECT HERE WILL REUSE OBJECT FOR MAP EVENT AND OTHER TABLES
		List<CacheVO> cacheImages = null;
		if(DashConstants.ALL.equalsIgnoreCase(channels)){
			cacheImages = new ArrayList<CacheVO>();
		}else{//reusing object
			cacheImages = cacheVOContainer.getCacheImages();
		}
		CacheVO cacheVO = null;
		int index = 0;
		int count;
		
		if(overlayBaseBookingsForManipulation!=null){ //when subsequent cycles are being invoked, NOT for first cycle
			if(channelSpecificPSPBookings.size()==0) {//setting same top10POS for that channel for all cacheImages since no sales for that channel
				DashLoggerUtil.log(Level.DEBUG, "no bookings for this channel phase 2 ::" + channels);
				if(DashConstants.ALL.equalsIgnoreCase(channels)){
					createTop10PosImage(overlayBaseBookingsForManipulation, channels, null, overlayComparisonBookingsForManipulation);
					cacheImages.add(cacheVO);
				}else {
					for(CacheVO cacheVO2 : cacheImages) {
						createTop10PosImage(overlayBaseBookingsForManipulation, channels, cacheVO2,overlayComparisonBookingsForManipulation);
					}
				}
			}else {
				for(SSRAndPaidSeatData deltaPspBooking : channelSpecificPSPBookings){
					
					overlayBaseBookingsForManipulation.add(deltaPspBooking);
					if(!DashConstants.ALL.equalsIgnoreCase(channels)){
						createTop10PosImage(overlayBaseBookingsForManipulation, channels, cacheImages.get(index), overlayComparisonBookingsForManipulation);
					}else{
						cacheVO = createTop10PosImage(overlayBaseBookingsForManipulation, channels, null , overlayComparisonBookingsForManipulation);
						cacheImages.add(cacheVO);
					}
					index++;
					//need to put last calculated top 10 for that channel in the rest of the cacheImages
					if(index==channelSpecificPSPBookings.size() && cacheImages.size()> channelSpecificPSPBookings.size()) { 
						for(count = index; count < cacheImages.size(); count++) {
							createTop10PosImage(overlayBaseBookingsForManipulation, channels, cacheImages.get(count), overlayComparisonBookingsForManipulation);
						}
						
					}
					
				}
			}
		}else{//Dash start up will have only one cache image
			
			if(!DashConstants.ALL.equalsIgnoreCase(channels)){//existing cacheVO object will be updated
				createTop10PosImage(channelSpecificPSPBookings, channels, cacheImages.get(0), overlayComparisonBookingsForManipulation);
				
			}else{//CacheImages array has just been created and cacheVO needs to be added
				cacheVO = createTop10PosImage(channelSpecificPSPBookings, channels,null, overlayComparisonBookingsForManipulation);
				cacheImages.add(cacheVO);
			}
			
		}
		

		for(CacheVO check : cacheImages) {
			if((channels.equalsIgnoreCase(DashConstants.MAND) && (check.getTop10POSMand()==null || check.getTop10POSMand().size()==0)) ||
			   (channels.equalsIgnoreCase(DashConstants.MIPD) && (check.getTop10POSMiph()==null || check.getTop10POSMiph().size()==0))|| 
			   (channels.equalsIgnoreCase(DashConstants.MOB) && (check.getTop10POSMob()==null || check.getTop10POSMob().size()==0)) || 
			   (channels.equalsIgnoreCase(DashConstants.WEB) && (check.getTop10POSWeb()==null || check.getTop10POSWeb().size()==0))) {
				
				DashLoggerUtil.log(Level.ERROR, "Functionality failure OR no data in database at all");
				
			}
			
		}
		
		cacheVOContainer.setOverlay(thisOverlay);
		cacheVOContainer.setMode(thisMode);
		//cacheVOContainer.setReadByUI(false);
		//cacheVOContainer.setLastLoadedTime();//will be set by DashCacheUtil
		//cacheVOContainer.setDelays(null);//no delays in initialization for now
		cacheVOContainer.setCacheImages(cacheImages);
				
//		DashCacheUtil.setDataInCache(thisMode+"-"+thisOverlay, cacheVOContainer, PSPBookingContainer.getLastLoaded());

	}


	private List<PSPBooking> deepCopyPSP(List<PSPBooking> pspBaseBookings) {
		
		if(pspBaseBookings==null) return null;
		
		List<PSPBooking> temp = new ArrayList<PSPBooking>();
		PSPBooking pspBookingNew = null;
		for(PSPBooking pspBooking :  pspBaseBookings) {
			
			pspBookingNew = new PSPBooking();
			try {
//				BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
				BeanUtils.copyProperties(pspBookingNew, pspBooking);
			} catch (IllegalAccessException e) {
				DashLoggerUtil.log(Level.ERROR, "Deep copy error");
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				DashLoggerUtil.log(Level.ERROR, "Deep copy error");
				e.printStackTrace();
			}
			temp.add(pspBookingNew);
		}
		
		return temp;
	}
	
	
	private List<SSRAndPaidSeatData> deepCopyCombined(List<SSRAndPaidSeatData> pspBaseBookings) {
		
		if(pspBaseBookings==null) return null;
		
		List<SSRAndPaidSeatData> temp = new ArrayList<SSRAndPaidSeatData>();
		SSRAndPaidSeatData pspBookingNew = null;
		for(SSRAndPaidSeatData pspBooking :  pspBaseBookings) {
			
			pspBookingNew = new SSRAndPaidSeatData();
			try {
//				BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
				BeanUtils.copyProperties(pspBookingNew, pspBooking);
			} catch (IllegalAccessException e) {
				DashLoggerUtil.log(Level.ERROR, "Deep copy error");
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				DashLoggerUtil.log(Level.ERROR, "Deep copy error");
				e.printStackTrace();
			}
			temp.add(pspBookingNew);
		}
		
		return temp;
	}
	
	
	private ArrayList<MonthlyVO> deepCopyMonthly(ArrayList<MonthlyVO> monthlyBreakDown) {
		
		if(monthlyBreakDown==null) return null;
		
		ArrayList<MonthlyVO> temp = new ArrayList<MonthlyVO>();
		MonthlyVO monthlyVONew = null;
		for(MonthlyVO monthlyVO :  monthlyBreakDown) {
			
			monthlyVONew = new MonthlyVO();
			try {
//				BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
				BeanUtils.copyProperties(monthlyVONew, monthlyVO);
			} catch (IllegalAccessException e) {
				DashLoggerUtil.log(Level.ERROR, "Deep copy error");
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				DashLoggerUtil.log(Level.ERROR, "Deep copy error");
				e.printStackTrace();
			}
			temp.add(monthlyVONew);
		}
		
		return temp;
	}
	
	private List<SeatSellSSR> deepCopySSR(List<SeatSellSSR> seatSellSSRs) {
		
		if(seatSellSSRs==null) return null;
		
		List<SeatSellSSR> temp = new ArrayList<SeatSellSSR>();
		SeatSellSSR ssrNew = null;
		for(SeatSellSSR seatSellSSR :  seatSellSSRs) {
			
			ssrNew = new SeatSellSSR();
			try {
				BeanUtils.copyProperties(ssrNew, seatSellSSR);
			} catch (IllegalAccessException e) {
				DashLoggerUtil.log(Level.ERROR, "Deep copy error");
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				DashLoggerUtil.log(Level.ERROR, "Deep copy error");
				e.printStackTrace();
			}
			temp.add(ssrNew);
		}
		
		return temp;
	}

	//CHANGED
	private List<SSRAndPaidSeatData> trimBookingsAsPerChannel(
			List<SSRAndPaidSeatData> pspBookings, List<SSRAndPaidSeatData> pspBaseBookings, List<SSRAndPaidSeatData> pspBaseComparisonBookings,
			String channels) {
		
		List<SSRAndPaidSeatData> trimmedBookingsPerChannel = new ArrayList<SSRAndPaidSeatData>();
		if(DashConstants.ALL.equalsIgnoreCase(channels)){//do not trim
			 trimmedBookingsPerChannel = pspBookings;
			 return trimmedBookingsPerChannel;
		}
		

		SSRAndPaidSeatData pspBookingBaseTemp = null;
		SSRAndPaidSeatData pspBookingComparisonTemp = null;
		
		if(pspBaseBookings != null)//below code should not be executed on dash start
		{
			for(Iterator<SSRAndPaidSeatData> iterator = pspBaseBookings.iterator(); iterator.hasNext();){
				pspBookingBaseTemp = iterator.next();
				if(!channels.equalsIgnoreCase(pspBookingBaseTemp.getChannel())){
					iterator.remove();
				}	
			}
		}
		
		if(pspBaseComparisonBookings != null)//below code should not be executed on dash start
		{
			for(Iterator<SSRAndPaidSeatData> iterator = pspBaseComparisonBookings.iterator(); iterator.hasNext();){
				pspBookingComparisonTemp = iterator.next();
				if(!channels.equalsIgnoreCase(pspBookingComparisonTemp.getChannel())){
					iterator.remove();
				}	
			}
		}

		for(SSRAndPaidSeatData pspBookingTemp : pspBookings){
			
			if(channels.equalsIgnoreCase(pspBookingTemp.getChannel())){
				trimmedBookingsPerChannel.add(pspBookingTemp);
			}	
			
		}

		
		return trimmedBookingsPerChannel;
	}

	//CHANGED
	private CacheVO createTop10PosImage(List<SSRAndPaidSeatData> pspBaseBookings, String channel, CacheVO cacheVO, List<SSRAndPaidSeatData> overlayComparisonBookingsForManipulation){
		
		
		LatLongVO countryEntity = null;
		if(cacheVO==null)//channels = ALL ; first invocation
		 cacheVO = new CacheVO();
		Double sumOfPSPRevenuesForAllCountries = 0d;
		Double sumOfPSPRevenuesForCurrentCountry;
		int numberOfPSPSeatsForCurrentCountry = 0;
		int numberOfPSPSeatsForAllCountries = 0;
		int numberOfPSPSeatsForCurrentCountryComparison = 0;
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		Date currentDate = new Date();
		cal1.setTime(currentDate);
		
		Double rateToAED = 1d;
		HashMap<String, Double> currencyMap = (HashMap<String, Double>) DashCacheUtil.getDataFromCache(DashConstants.CURRENCY_RATES_TABLE);
		
//		int sectorsInCurrentReportBooking;
//		int numberOfSeatsInCurrentReportBooking;
		
		//Country Key String like 'AE'
		Map<String,Double> sumOfPSPRevenuesCountryMap = new HashMap<String, Double>();
		//Map<String,Double> sumOfRevenuesCountryMap = new HashMap<String, Double>();
		Map<String, Integer> numberOfPSPSeatsCountryMap = new HashMap<String, Integer>();
		//Map<String, Integer> numberOfSeatsCountryMap = new HashMap<String, Integer>();
		Map<String, Integer> numberOfPSPSeatsCountryMapComparison = new HashMap<String, Integer>();
		
		List<Top10PosVO> top10PosVOList = new ArrayList<Top10PosVO>();
		Top10PosVO top10PosVO = null;
		

		for(SSRAndPaidSeatData pspb : pspBaseBookings){
			// not multiplying by number of Adults currently
			rateToAED = currencyMap.get(pspb.getCurrencyCode());
			if(rateToAED==null)rateToAED = 1d;
			
			if(null!=sumOfPSPRevenuesCountryMap.get(pspb.getPosCntry())) {
//				sumOfPSPRevenuesForCurrentCountry = sumOfPSPRevenuesCountryMap.get(pspb.getPosCntry()) + (pspb.getAdultFare()==null?0:pspb.getAdultFare().doubleValue()*rateToAED) +
//                        (pspb.getOfwFare()==null?0:pspb.getOfwFare().doubleValue()*rateToAED) + (pspb.getChildFare()==null?0:pspb.getChildFare().doubleValue()*rateToAED) +
//                        (pspb.getTeenagerFare()==null?0:pspb.getTeenagerFare().doubleValue()*rateToAED);
				
				sumOfPSPRevenuesForCurrentCountry = sumOfPSPRevenuesCountryMap.get(pspb.getPosCntry()) + Double.valueOf(pspb.getBaseFare()==null||pspb.getBaseFare().equalsIgnoreCase("")?"0":pspb.getBaseFare())*rateToAED +
					 Double.valueOf(pspb.getExchangedFare()==null||pspb.getExchangedFare().equalsIgnoreCase("")?"0":pspb.getExchangedFare())*rateToAED;	
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				sumOfPSPRevenuesForCurrentCountry = sumOfPSPRevenuesCountryMap.get(pspb.getPosCntry()) + Double.valueOf(pspb.getBaseFare()==null||pspb.getBaseFare().equalsIgnoreCase("")?"0":pspb.getBaseFare())*rateToAED +
//						 Double.valueOf(pspb.getExchangedFare()==null||pspb.getExchangedFare().equalsIgnoreCase("")?"0":pspb.getExchangedFare())*rateToAED +
//						 Double.valueOf(pspb.getTax()==null?0:pspb.getTax().doubleValue()*rateToAED);	
				
//				sumOfPSPRevenuesForCurrentCountry = sumOfPSPRevenuesCountryMap.get(pspb.getPosCntry()) + (pspb.getTotal()==null?0:pspb.getTotal().doubleValue()*rateToAED)+
//						 (pspb.getTotalTax()==null?0:pspb.getTotalTax().doubleValue()*rateToAED);	
				
			}else {
//				sumOfPSPRevenuesForCurrentCountry = (pspb.getAdultFare()==null?0:pspb.getAdultFare().doubleValue()*rateToAED) +
//                        (pspb.getOfwFare()==null?0:pspb.getOfwFare().doubleValue()*rateToAED) + (pspb.getChildFare()==null?0:pspb.getChildFare().doubleValue()*rateToAED) +
//                        (pspb.getTeenagerFare()==null?0:pspb.getTeenagerFare().doubleValue()*rateToAED);
				
				sumOfPSPRevenuesForCurrentCountry = Double.valueOf(pspb.getBaseFare()==null||pspb.getBaseFare().equalsIgnoreCase("")?"0":pspb.getBaseFare())*rateToAED +
						 Double.valueOf(pspb.getExchangedFare()==null||pspb.getExchangedFare().equalsIgnoreCase("")?"0":pspb.getExchangedFare())*rateToAED;	
				
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				sumOfPSPRevenuesForCurrentCountry = Double.valueOf(pspb.getBaseFare()==null||pspb.getBaseFare().equalsIgnoreCase("")?"0":pspb.getBaseFare())*rateToAED +
//						 Double.valueOf(pspb.getExchangedFare()==null||pspb.getExchangedFare().equalsIgnoreCase("")?"0":pspb.getExchangedFare())*rateToAED +
//						 Double.valueOf(pspb.getTax()==null?0:pspb.getTax().doubleValue()*rateToAED);	
				
//				sumOfPSPRevenuesForCurrentCountry = (pspb.getTotal()==null?0:pspb.getTotal().doubleValue()*rateToAED)+
//						 (pspb.getTotalTax()==null?0:pspb.getTotalTax().doubleValue()*rateToAED);	
			
			}	
			sumOfPSPRevenuesCountryMap.put(pspb.getPosCntry(), sumOfPSPRevenuesForCurrentCountry);
			
			if(null!=numberOfPSPSeatsCountryMap.get(pspb.getPosCntry()))
				numberOfPSPSeatsForCurrentCountry = numberOfPSPSeatsCountryMap.get(pspb.getPosCntry()).intValue() + 1;//pspb.getTotalPax();
			else
				numberOfPSPSeatsForCurrentCountry = 1;//pspb.getTotalPax();
			numberOfPSPSeatsCountryMap.put(pspb.getPosCntry(), numberOfPSPSeatsForCurrentCountry);
			
			numberOfPSPSeatsForAllCountries += 1;//pspb.getTotalPax();
//			numberOfPSPSeatsForAllCountries +=  1;
			sumOfPSPRevenuesForAllCountries += Double.valueOf(pspb.getBaseFare()==null||pspb.getBaseFare().equalsIgnoreCase("")?"0":pspb.getBaseFare())*rateToAED +
					 Double.valueOf(pspb.getExchangedFare()==null||pspb.getExchangedFare().equalsIgnoreCase("")?"0":pspb.getExchangedFare())*rateToAED;	
			//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//			sumOfPSPRevenuesForAllCountries += Double.valueOf(pspb.getBaseFare()==null||pspb.getBaseFare().equalsIgnoreCase("")?"0":pspb.getBaseFare())*rateToAED +
//					 Double.valueOf(pspb.getExchangedFare()==null||pspb.getExchangedFare().equalsIgnoreCase("")?"0":pspb.getExchangedFare())*rateToAED+
//					 Double.valueOf(pspb.getTax()==null?0:pspb.getTax().doubleValue()*rateToAED);	
			

//			sumOfPSPRevenuesForAllCountries += (pspb.getTotal()==null?0:pspb.getTotal().doubleValue()*rateToAED)+
//					 (pspb.getTotalTax()==null?0:pspb.getTotalTax().doubleValue()*rateToAED);
			
		}
		
		
		for(SSRAndPaidSeatData pspbComp : overlayComparisonBookingsForManipulation){
			cal2.clear();
			cal2.setTime(pspbComp.getTransDate());
			if((cal1.get(Calendar.HOUR_OF_DAY)<cal2.get(Calendar.HOUR_OF_DAY)) ||
			   (cal1.get(Calendar.HOUR_OF_DAY)==cal2.get(Calendar.HOUR_OF_DAY) && cal1.get(Calendar.MINUTE) < cal2.get(Calendar.MINUTE)) ||
			   (cal1.get(Calendar.HOUR_OF_DAY)==cal2.get(Calendar.HOUR_OF_DAY) && cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) && cal1.get(Calendar.SECOND) <cal2.get(Calendar.SECOND)) ||
			   (cal1.get(Calendar.HOUR_OF_DAY)==cal2.get(Calendar.HOUR_OF_DAY) && cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) && cal1.get(Calendar.SECOND) ==cal2.get(Calendar.SECOND) && cal1.get(Calendar.MILLISECOND) < cal2.get(Calendar.MILLISECOND))) {
				
				//using continue because there is no guartanee that order will remain same over a few days in hashmap/list?
				continue;
			}
			
			
			rateToAED = currencyMap.get(pspbComp.getCurrencyCode());
			if(rateToAED==null)rateToAED = 1d;
			
			if(null!=numberOfPSPSeatsCountryMapComparison.get(pspbComp.getPosCntry()))
				numberOfPSPSeatsForCurrentCountryComparison = numberOfPSPSeatsCountryMapComparison.get(pspbComp.getPosCntry()).intValue() + 1;//pspbComp.getTotalPax();
			else
				numberOfPSPSeatsForCurrentCountryComparison = 1;//pspbComp.getTotalPax();
			numberOfPSPSeatsCountryMapComparison.put(pspbComp.getPosCntry(), numberOfPSPSeatsForCurrentCountryComparison);
			
		}
		
		
		sumOfPSPRevenuesCountryMap = MapSortUtil.sortByValueDescending(sumOfPSPRevenuesCountryMap);
		//sumOfRevenuesCountryMap =  MapSortUtil.sortByValueDescending(sumOfRevenuesCountryMap);
		numberOfPSPSeatsCountryMap = MapSortUtil.sortByValueDescending(numberOfPSPSeatsCountryMap);
		//numberOfSeatsCountryMap = MapSortUtil.sortByValueDescending(numberOfSeatsCountryMap);
		numberOfPSPSeatsCountryMapComparison = MapSortUtil.sortByValueDescending(numberOfPSPSeatsCountryMapComparison);
		
		//DISCLAIMER : each of the below represent one distinct ordering of top 10 POS if requirement is that user is able order by column 
		//List<Map.Entry<String, Double>> listPSPRevenuesByCountry = new LinkedList<Map.Entry<String, Double>>(sumOfPSPRevenuesCountryMap.entrySet());
		//List<Map.Entry<String, Double>> listRevenuesPerCountry = new LinkedList<Map.Entry<String, Double>>(sumOfPSPRevenuesCountryMap.entrySet());
		List<Map.Entry<String, Integer>> listPSPSeatsByCountry = new LinkedList<Map.Entry<String, Integer>>(numberOfPSPSeatsCountryMap.entrySet());
		//List<Map.Entry<String, Integer>> listSeatsPerCountry = new LinkedList<Map.Entry<String, Integer>>(numberOfSeatsCountryMap.entrySet());
		List<Map.Entry<String, Integer>> listPSPSeatsByCountryComparison = new LinkedList<Map.Entry<String, Integer>>(numberOfPSPSeatsCountryMapComparison.entrySet());

		MasterResponseDTO locationsMapContainer = (MasterResponseDTO)DashCacheUtil.getDataFromCache(DashConstants.LOCATIONS_CACHE_KEY);
		//creating top 10 based on PSP seats sold only...not for orderings of other columns TODO
		//only doing USD since currency conversion not yet ready
		for(int i =0; i<listPSPSeatsByCountry.size(); i++){

			if(i==10) break;
			
			top10PosVO = new Top10PosVO();
			top10PosVO.setPosCountryCode(listPSPSeatsByCountry.get(i).getKey());
			countryEntity = locationsMapContainer.getCountries().get(listPSPSeatsByCountry.get(i).getKey());
			if(countryEntity == null) {
				countryEntity = new LatLongVO();
				countryEntity.setCountry(listPSPSeatsByCountry.get(i).getKey());
			}
			top10PosVO.setPosCountryForDisplay(countryEntity.getCountry());//need to convert from country code to country name
			top10PosVO.setBookings(listPSPSeatsByCountry.get(i).getValue());
			//top10PosVO.setRevenue(listPSPRevenuesPerCountry.get(i).getValue()); <- Please note that THIS IS WRONG FOR PSP SEATS SOLD BASED ORDERING 
			top10PosVO.setRevenue(sumOfPSPRevenuesCountryMap.get((listPSPSeatsByCountry.get(i).getKey())));//CORRECT
			top10PosVO.setPercentageOfTotalRevenue(sumOfPSPRevenuesCountryMap.get((listPSPSeatsByCountry.get(i).getKey())).doubleValue() / 
					                               sumOfPSPRevenuesForAllCountries  * 100);
			top10PosVO.setPercentageOfTotal(Double.valueOf(String.valueOf(
					                         numberOfPSPSeatsCountryMap.get(listPSPSeatsByCountry.get(i).getKey()).doubleValue()/ 
					                         numberOfPSPSeatsForAllCountries * 100.0)));
			if(null!=numberOfPSPSeatsCountryMapComparison.get(listPSPSeatsByCountry.get(i).getKey())) {
				top10PosVO.setChange((numberOfPSPSeatsCountryMap.get(listPSPSeatsByCountry.get(i).getKey()).doubleValue() - numberOfPSPSeatsCountryMapComparison.get(listPSPSeatsByCountry.get(i).getKey()).doubleValue()) / 
								  numberOfPSPSeatsCountryMapComparison.get(listPSPSeatsByCountry.get(i).getKey()).doubleValue() * 100);
			}else {
				top10PosVO.setChange(0d);
				if("ALL".equalsIgnoreCase(channel)) {
					DashLoggerUtil.log(Level.ERROR,"Change percentage calculation issue::"+listPSPSeatsByCountry.size()+"::"+numberOfPSPSeatsCountryMapComparison.size() + "::" + numberOfPSPSeatsCountryMap.size()+
						"::" + overlayComparisonBookingsForManipulation.size() + "::" + pspBaseBookings.size()+"::"+channel);
				}	
			}		
			top10PosVOList.add(top10PosVO);

			
		}
		
		switch(channel){
		
		case "ALL" 	: cacheVO.setTop10POSAll(top10PosVOList) ;break;
		case "WEB" 	: cacheVO.setTop10POSWeb(top10PosVOList) ;break;
		case "MOB" 	: cacheVO.setTop10POSMob(top10PosVOList) ;break;
		case "MAND" : cacheVO.setTop10POSMand(top10PosVOList);break;
		case "MIPH" : cacheVO.setTop10POSMiph(top10PosVOList);break;
		default : break;
			
		
		}
		
		//clearing all used maps for GC
		numberOfPSPSeatsCountryMapComparison = null;
		currencyMap = null;
		numberOfPSPSeatsCountryMap = null;
		sumOfPSPRevenuesCountryMap = null;
		listPSPSeatsByCountry = null;
		listPSPSeatsByCountryComparison = null;
		
		return cacheVO;
				
	}
	
	//unused
    private void trimDataAsPerOverlay(Overlay thisOverlay,
			List<SSRAndPaidSeatData> pspBaseBookings) {
		// TODO Auto-generated method stub
		
	}

    /*FOR CITY POS
	public void populatePOSActivityForMap(PSPBookingContainer pspbc,
		CacheVOContainer cacheVOContainer, Mode thisMode,
		Overlay thisOverlay, List<Long> delays){
		
		MasterResponseDTO locationsMapContainer = (MasterResponseDTO)DashCacheUtil.getDataFromCache(DashConstants.LOCATIONS_CACHE_KEY);
		//if delays are null, then only one cacheImage and this is the startup call;
		MapEventVO posActivity = null;
		List<CacheVO> cacheImages = null;
		
		if(cacheVOContainer.getCacheImages()!=null){
			cacheImages = cacheVOContainer.getCacheImages();
		}else{
		    cacheImages = new ArrayList<CacheVO>();
		}

		int delayIndex = 0; //index into delay array & cacheimages

		if (delays!=null){ //not the first time invocation, i.e not the startup
			for(SSRAndPaidSeatData pspBooking : pspbc.getPspBookingsNew()){
	
				//data required in mapEventVo is subject to change
				posActivity = new MapEventVO();
				posActivity.setPos(pspBooking.getOrigin());
				posActivity.setTransactions(1);//pspBooking.getTotalPax());
				try {
					posActivity.setLatitude(Double.valueOf(locationsMapContainer.getEKLocations().get(pspBooking.getOrigin()).getLatitudeCity()));
					posActivity.setLongitude(Double.valueOf(locationsMapContainer.getEKLocations().get(pspBooking.getOrigin()).getLongitudeCity()));
				}catch(Exception e){
					e.printStackTrace();
					DashLoggerUtil.log(Level.ERROR, "Locations xml doesnt have lat long");
				}
				try{
				   if(cacheImages.size()>delayIndex){
					   if(cacheImages.get(delayIndex).getPosActivity()==null){
						   cacheImages.get(delayIndex).setPosActivity(new ArrayList<MapEventVO>());
					   }
				     cacheImages.get(delayIndex).getPosActivity().add(posActivity);
				   }

				if(delayIndex+1<delays.size() && delays.get(delayIndex).intValue()==delays.get(delayIndex+1).intValue()){
					delays.remove(delayIndex);
				}else{
					delayIndex++;
				}				

				}catch(Exception e){
					e.printStackTrace();
					DashLoggerUtil.log(Level.ERROR, "Index Out Of bounds run time error");
				}
	
			}
		

			//sum all POS activity for each cacheImage for a particular delay
			posActivity = null;
			List<MapEventVO> collatedPosActivities = null;
			Map<String, Integer> numberOfTransactionsPerPosMap = null;
		    int temp;
		    List<Map.Entry<String, Integer>> transactionsPerPosForOneDelay = null;
			
			for(CacheVO cacheVO : cacheVOContainer.getCacheImages()){
				
				numberOfTransactionsPerPosMap = new HashMap<String, Integer>();	
				collatedPosActivities = new ArrayList<MapEventVO>();
				for(MapEventVO mapEvent : cacheVO.getPosActivity()){
					temp = 0;
					if(null!=numberOfTransactionsPerPosMap.get(mapEvent.getPos()))
						temp = numberOfTransactionsPerPosMap.get(mapEvent.getPos()).intValue() + mapEvent.getTransactions();
					else
						temp = mapEvent.getTransactions();
					numberOfTransactionsPerPosMap.put(mapEvent.getPos(), temp);
				  
				}
				transactionsPerPosForOneDelay = new LinkedList<Map.Entry<String, Integer>>(numberOfTransactionsPerPosMap.entrySet());
				for(int i = 0; i<transactionsPerPosForOneDelay.size(); i++){
					
					posActivity = new MapEventVO();
					posActivity.setPos(locationsMapContainer.getEKLocations().get(transactionsPerPosForOneDelay.get(i).getKey()).getCity());
					posActivity.setTransactions(transactionsPerPosForOneDelay.get(i).getValue());
					posActivity.setLatitude(Double.valueOf(locationsMapContainer.getEKLocations().get(transactionsPerPosForOneDelay.get(i).getKey()).getLatitudeCity()));
					posActivity.setLongitude(Double.valueOf(locationsMapContainer.getEKLocations().get(transactionsPerPosForOneDelay.get(i).getKey()).getLongitudeCity()));
					collatedPosActivities.add(posActivity);
					
				}
				cacheVO.setPosActivity(collatedPosActivities);
				
			}
		
		}
		
		cacheVOContainer.setOverlay(thisOverlay);
		cacheVOContainer.setMode(thisMode);
//		cacheVOContainer.setReadByUI(false);
		cacheVOContainer.setCacheImages(cacheImages);
		
		//DashCacheUtil.setDataInCache(thisMode+"-"+thisOverlay, cacheVOContainer, PSPBookingContainer.getLastLoaded());		
			
	}
     */
    //FOR COUNTRY AGGREGATION
    public void populatePOSActivityForMap(PSPBookingContainer pspbc,
    		CacheVOContainer cacheVOContainer, Mode thisMode,
    		Overlay thisOverlay, List<Long> delays){
    	
    		Boolean AbsentInLocationXML = false;
    		
    		MasterResponseDTO locationsMapContainer = (MasterResponseDTO)DashCacheUtil.getDataFromCache(DashConstants.LOCATIONS_CACHE_KEY);
    		//if delays are null, then only one cacheImage and this is the startup call;
    		MapEventVO posActivity = null;
    		List<CacheVO> cacheImages = null;
    		
    		if(cacheVOContainer.getCacheImages()!=null){
    			cacheImages = cacheVOContainer.getCacheImages();
    		}else{
    		    cacheImages = new ArrayList<CacheVO>();
    		}

    		int delayIndex = 0; //index into delay array & cacheimages

    		if (delays!=null){ //not the first time invocation, i.e not the startup
    			for(SSRAndPaidSeatData pspBooking : pspbc.getPspBookingsNew()){
    	
    				//data required in mapEventVo is subject to change
    				posActivity = new MapEventVO();
    				posActivity.setPos(pspBooking.getPosCntry());
//    				if(locationsMapContainer==null) {
//    					DashLoggerUtil.log(Level.INFO,"Location xml cache erased 1 ");
//    		    		locationsMapContainer = (MasterResponseDTO)DashCacheUtil.getDataFromCache(DashConstants.LOCATIONS_CACHE_KEY);
//    		    		if(locationsMapContainer==null) {
//    		    			DashLoggerUtil.log(Level.INFO,"Location xml cache erased 2");
//    		    			locationsMapContainer = LocationXMLReader.retrieveLocationsJSON();
//    		    		}
//    				}
    				try {
    					posActivity.setPosName(locationsMapContainer.getCountries().get(pspBooking.getPosCntry()).getCountry());
    				}catch(Exception e) {
    					
    					DashLoggerUtil.log(Level.ERROR,"POS country error");
    					DashLoggerUtil.log(Level.ERROR,"locationsMapContainer.getCountries().get(pspBooking.getPosCntry()) is null:::"+pspBooking.getPosCntry());
    					DashLoggerUtil.log(Level.ERROR,"locations xml parsing failure or POS doesnt exist in location xml:::"+pspBooking.getPosCntry());
    					posActivity.setPosName("United Arab Emirates");	
    					AbsentInLocationXML = true;
	
    				}
    				posActivity.setTransactions(1);
    				try {
    					if(AbsentInLocationXML) {
    						posActivity.setLatitude(Double.valueOf(locationsMapContainer.getCountries().get("AE").getLatitudeCountry()));
        					posActivity.setLongitude(Double.valueOf(locationsMapContainer.getCountries().get("AE").getLongitudeCountry()));
    					}else {
    						posActivity.setLatitude(Double.valueOf(locationsMapContainer.getCountries().get(pspBooking.getPosCntry()).getLatitudeCountry()));
        					posActivity.setLongitude(Double.valueOf(locationsMapContainer.getCountries().get(pspBooking.getPosCntry()).getLongitudeCountry()));
    					}
    						
    					
    				}catch(Exception e){
    					e.printStackTrace();
    					DashLoggerUtil.log(Level.ERROR, "Locations xml doesnt have lat long");
    				}
    				try{
    				   if(cacheImages.size()>delayIndex){
    					   if(cacheImages.get(delayIndex).getPosActivity()==null){
    						   cacheImages.get(delayIndex).setPosActivity(new ArrayList<MapEventVO>());
    					   }
    				     cacheImages.get(delayIndex).getPosActivity().add(posActivity);
    				   }

    				//Number of entries in Delays list will now be same as number of cache images, cache images were condensed using below logic in cleanCacheContainer method   
    				if(delayIndex+1<delays.size() && delays.get(delayIndex).intValue()==delays.get(delayIndex+1).intValue()){
    					delays.remove(delayIndex);
    				}else{
    					delayIndex++;
    				}				

    				}catch(Exception e){
    					e.printStackTrace();
    					DashLoggerUtil.log(Level.ERROR, "Index Out Of bounds run time error");
    				}
    	
    			}
    		

    			//sum all POS activity for a particular POS for each cacheImage/delay, cache images and delays are now same in number.
    			posActivity = null;
    			List<MapEventVO> collatedPosActivities = null;
    			Map<String, Integer> numberOfTransactionsPerPosMap = null;
    		    int temp;
    		    List<Map.Entry<String, Integer>> transactionsPerPosForOneDelay = null;
    			
    			for(CacheVO cacheVO : cacheVOContainer.getCacheImages()){
    				
    				numberOfTransactionsPerPosMap = new HashMap<String, Integer>();	
    				collatedPosActivities = new ArrayList<MapEventVO>();
    				for(MapEventVO mapEvent : cacheVO.getPosActivity()){
    					temp = 0;
    					if(null!=numberOfTransactionsPerPosMap.get(mapEvent.getPos()))
    						temp = numberOfTransactionsPerPosMap.get(mapEvent.getPos()).intValue() + mapEvent.getTransactions();
    					else
    						temp = mapEvent.getTransactions();
    					numberOfTransactionsPerPosMap.put(mapEvent.getPos(), temp);
    				  
    				}
    				transactionsPerPosForOneDelay = new LinkedList<Map.Entry<String, Integer>>(numberOfTransactionsPerPosMap.entrySet());
    				for(int i = 0; i<transactionsPerPosForOneDelay.size(); i++){
    					
    					posActivity = new MapEventVO();
    					posActivity.setPos(transactionsPerPosForOneDelay.get(i).getKey());
    					try {
    						posActivity.setPosName(locationsMapContainer.getCountries().get(transactionsPerPosForOneDelay.get(i).getKey()).getCountry());
    						posActivity.setLatitude(Double.valueOf(locationsMapContainer.getCountries().get(transactionsPerPosForOneDelay.get(i).getKey()).getLatitudeCountry()));
    						posActivity.setLongitude(Double.valueOf(locationsMapContainer.getCountries().get(transactionsPerPosForOneDelay.get(i).getKey()).getLongitudeCountry()));
    						
    					}catch(Exception ee) {
    						DashLoggerUtil.log(Level.ERROR,"locations xml parsing failure or POS doesnt exist in location xml::"+transactionsPerPosForOneDelay.get(i).getKey());
    						posActivity.setPosName("United Arab Emirates");
    						posActivity.setLatitude(Double.valueOf(locationsMapContainer.getCountries().get("AE").getLatitudeCountry()));
    						posActivity.setLongitude(Double.valueOf(locationsMapContainer.getCountries().get("AE").getLongitudeCountry()));
    					}
    					posActivity.setTransactions(transactionsPerPosForOneDelay.get(i).getValue());
    					collatedPosActivities.add(posActivity);
    					
    				}
    				cacheVO.setPosActivity(collatedPosActivities);
    				
    				//clearing maps
    				//collatedPosActivities.clear();
    				numberOfTransactionsPerPosMap.clear();
    				//transactionsPerPosForOneDelay.clear();
    				
    			}
    			
    			//available for GC
    			collatedPosActivities = null;
    			numberOfTransactionsPerPosMap = null;
    		    transactionsPerPosForOneDelay = null;
    		
    		}
    		
    		cacheVOContainer.setOverlay(thisOverlay);
    		cacheVOContainer.setMode(thisMode);
//    		cacheVOContainer.setReadByUI(false);
    		cacheVOContainer.setCacheImages(cacheImages);
    		
    		//DashCacheUtil.setDataInCache(thisMode+"-"+thisOverlay, cacheVOContainer, PSPBookingContainer.getLastLoaded());		
    		

    			
    	}
    
    
	public void initializeChannelStats(PSPBookingContainer pspbc,
			CacheVOContainer cacheVOContainer, Mode thisMode,
			Overlay thisOverlay, List<SSRAndPaidSeatData> pspBaseBookingsForOverlay) {

		boolean dashStartUp = false;
		List<SSRAndPaidSeatData> overlayBaseBookingsForManipulation = deepCopyCombined(pspBaseBookingsForOverlay);
		List<SSRAndPaidSeatData> pspBookingsForManipulations = deepCopyCombined(pspbc.getPspBookingsNew());

		if(overlayBaseBookingsForManipulation == null){
			overlayBaseBookingsForManipulation = pspBookingsForManipulations;
			dashStartUp = true;
		}
		
		int index = 0;
		for(CacheVO cacheVO : cacheVOContainer.getCacheImages()){

			if(dashStartUp){
				updateCacheImageWithChannelStats(cacheVO, overlayBaseBookingsForManipulation);
			}else{
				overlayBaseBookingsForManipulation.add(pspBookingsForManipulations.get(index));
				updateCacheImageWithChannelStats(cacheVO, overlayBaseBookingsForManipulation);
				index++;
			}
	
		}
	
	}

	private void updateCacheImageWithChannelStats(CacheVO cacheVO,
			List<SSRAndPaidSeatData> pspBaseBookingsForOverlay) {

		List<ChannelVO> channelList = new ArrayList<ChannelVO>();
		ChannelVO channelVO = null;
		int sumOfSeatsForAllChannels = 0;
		Double sumOfRevenuesForAllChannels = 0d;
		Map<String, Integer> mapOfSeatsPerChannel = new HashMap<String, Integer>();
		Map<String, Double> mapOfRevenuesPerChannel = new HashMap<String, Double>();
		int numberOfSeatsForCurrentChannelTEMP = 0;
		Double revenueForCurrentChannelTEMP = 0d;
		HashMap<String, Double> currencyMap = (HashMap<String, Double>) DashCacheUtil.getDataFromCache(DashConstants.CURRENCY_RATES_TABLE);
		Double rateToAED = 1d;
		
		for(SSRAndPaidSeatData pspBooking : pspBaseBookingsForOverlay) {
			rateToAED = currencyMap.get(pspBooking.getCurrencyCode());
			if(rateToAED==null)rateToAED = 1d;
			
			if(null!=mapOfSeatsPerChannel.get(pspBooking.getChannel())){
				numberOfSeatsForCurrentChannelTEMP  = mapOfSeatsPerChannel.get(pspBooking.getChannel()).intValue() + 1;// pspBooking.getTotalPax();
			}else{
				numberOfSeatsForCurrentChannelTEMP  = 1;//pspBooking.getTotalPax();
			}
			mapOfSeatsPerChannel.put(pspBooking.getChannel(),numberOfSeatsForCurrentChannelTEMP);

			if(null!=mapOfRevenuesPerChannel.get(pspBooking.getChannel())){
//				revenueForCurrentChannelTEMP = mapOfRevenuesPerChannel.get(pspBooking.getChannel()).doubleValue() + (pspBooking.getAdultFare()==null?0:pspBooking.getAdultFare().doubleValue()*rateToAED) +
//	                    						(pspBooking.getOfwFare()==null?0:pspBooking.getOfwFare().doubleValue()*rateToAED) + (pspBooking.getChildFare()==null?0:pspBooking.getChildFare().doubleValue()*rateToAED) +
//	                    						(pspBooking.getTeenagerFare()==null?0:pspBooking.getTeenagerFare().doubleValue()*rateToAED);
				revenueForCurrentChannelTEMP = mapOfRevenuesPerChannel.get(pspBooking.getChannel()).doubleValue() + 
												Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
												Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
				
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentChannelTEMP = mapOfRevenuesPerChannel.get(pspBooking.getChannel()).doubleValue() + 
//						Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//						Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+
//						Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);	
				

//				revenueForCurrentChannelTEMP = mapOfRevenuesPerChannel.get(pspBooking.getChannel()).doubleValue() +  (pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//				 (pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
				
			}else{
//				revenueForCurrentChannelTEMP = (pspBooking.getAdultFare()==null?0:pspBooking.getAdultFare().doubleValue()*rateToAED) +
//						(pspBooking.getOfwFare()==null?0:pspBooking.getOfwFare().doubleValue()*rateToAED) + (pspBooking.getChildFare()==null?0:pspBooking.getChildFare().doubleValue()*rateToAED) +
//						(pspBooking.getTeenagerFare()==null?0:pspBooking.getTeenagerFare().doubleValue()*rateToAED);
				
				revenueForCurrentChannelTEMP = 	Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
												Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
				
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentChannelTEMP = 	Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//						Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+
//						Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
				
//				revenueForCurrentChannelTEMP = (pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//						 (pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
				
				
			}
			mapOfRevenuesPerChannel.put(pspBooking.getChannel(), revenueForCurrentChannelTEMP);
				
		    sumOfSeatsForAllChannels+= 1;//pspBooking.getTotalPax();
			sumOfRevenuesForAllChannels+=Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
										 Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
			
			//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//			sumOfRevenuesForAllChannels+=Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//					 Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+
//					 Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
			
//			sumOfRevenuesForAllChannels+=(pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//					 (pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
			
			
		}
		
		mapOfRevenuesPerChannel = MapSortUtil.sortByValueDescending(mapOfRevenuesPerChannel);
		mapOfSeatsPerChannel = MapSortUtil.sortByValueDescending(mapOfSeatsPerChannel);
		//Ordering for UI is only by seats
		List<Map.Entry<String, Integer>> listPSPSeatsByChannel = new LinkedList<Map.Entry<String, Integer>>(mapOfSeatsPerChannel.entrySet());
		
		for(int i = 0; i<listPSPSeatsByChannel.size(); i++){
			if(null!=listPSPSeatsByChannel && null!=listPSPSeatsByChannel.get(i)){
				channelVO = new ChannelVO();
				switch(listPSPSeatsByChannel.get(i).getKey()){
				
					case DashConstants.WEB : channelVO.setChannel("Web");break;
					case DashConstants.MOB : channelVO.setChannel("Mobile");break;
					case DashConstants.MAND : channelVO.setChannel("Android");break;
					case DashConstants.MIPH : channelVO.setChannel("iPhone");break;
					default : channelVO.setChannel("Web"); break;
				
				}
				channelVO.setPercentage(mapOfSeatsPerChannel.get(listPSPSeatsByChannel.get(i).getKey()).doubleValue() / 
										sumOfSeatsForAllChannels * 100);
				channelVO.setSeats(mapOfSeatsPerChannel.get(listPSPSeatsByChannel.get(i).getKey()));
				
				channelVO.setRevenue(mapOfRevenuesPerChannel.get(listPSPSeatsByChannel.get(i).getKey()));
				channelVO.setRevenuePercentage(mapOfRevenuesPerChannel.get(listPSPSeatsByChannel.get(i).getKey()).doubleValue() /
						                       sumOfRevenuesForAllChannels * 100);
				channelList.add(channelVO);
			}
			
			
		}
		
		sortChannelsForUIRequirement(channelList);
		
		cacheVO.setChannelSplit(channelList);
		
		//clearing maps 
		listPSPSeatsByChannel = null;
		currencyMap = null;
		mapOfSeatsPerChannel = null;
		mapOfRevenuesPerChannel = null;
	}

	private void sortChannelsForUIRequirement(List<ChannelVO> channelList) {
		
		boolean isWebPresent = false;
		boolean isMobPresent = false;
		boolean isMandPresent = false;
		boolean isMiphPresent = false;
		ChannelVO channelVO = null;
		
		for(int i = 0; i <channelList.size() ; i++) {
			
			if("Web".equalsIgnoreCase(channelList.get(i).getChannel())) {
				isWebPresent = true;
			}
			if("Mobile".equalsIgnoreCase(channelList.get(i).getChannel())) {
				isMobPresent = true;
			}
			if("Android".equalsIgnoreCase(channelList.get(i).getChannel())) {
				isMandPresent = true;
			}
			if("iPhone".equalsIgnoreCase(channelList.get(i).getChannel())) {
				isMiphPresent = true;
			}
			
		}
		if(!isWebPresent){
			channelVO = new ChannelVO();
			channelVO.setChannel("Web");
			channelVO.setPercentage(0d);
			channelVO.setRevenue(0d);
			channelVO.setRevenuePercentage(0d);
			channelVO.setSeats(0);
			
			channelList.add(channelVO);
		}
		if(!isMobPresent){
			
			channelVO = new ChannelVO();
			channelVO.setChannel("Mobile");
			channelVO.setPercentage(0d);
			channelVO.setRevenue(0d);
			channelVO.setRevenuePercentage(0d);
			channelVO.setSeats(0);
			channelList.add(channelVO);
		}
		if(!isMandPresent){
			
			channelVO = new ChannelVO();
			channelVO.setChannel("Android");
			channelVO.setPercentage(0d);
			channelVO.setRevenue(0d);
			channelVO.setRevenuePercentage(0d);
			channelVO.setSeats(0);
			channelList.add(channelVO);
		}
		if(!isMiphPresent){
			
			channelVO = new ChannelVO();
			channelVO.setChannel("iPhone");
			channelVO.setPercentage(0d);
			channelVO.setRevenue(0d);
			channelVO.setRevenuePercentage(0d);
			channelVO.setSeats(0);
			channelList.add(channelVO);
		}
		
		
		for(int i = 0; i <channelList.size() ; i++) {
			
			if("Web".equalsIgnoreCase(channelList.get(i).getChannel())) {
				
				Collections.swap(channelList, i, 0);
			    	
			}
			
			if("Mobile".equalsIgnoreCase(channelList.get(i).getChannel())) {
				
				Collections.swap(channelList, i, 1);
		    	
			}
			if("iPhone".equalsIgnoreCase(channelList.get(i).getChannel())) {
				
				Collections.swap(channelList, i, 2);
		    	
			}
			if("Android".equalsIgnoreCase(channelList.get(i).getChannel())) {
				
				Collections.swap(channelList, i, 3);		    	
			}	
		}
	
	}

	public void initializeOverallStats(PSPBookingContainer pspbc,
			CacheVOContainer cacheVOContainer, Mode thisMode,
			Overlay thisOverlay, List<SSRAndPaidSeatData> pspBaseBookingsForOverlay, List<SSRAndPaidSeatData> combinedDataListForComparison) {
		
		
		boolean dashStartUp = false;
		List<SSRAndPaidSeatData> overlayBaseBookingsForManipulation = deepCopyCombined(pspBaseBookingsForOverlay);
		List<SSRAndPaidSeatData> overlayBaseBookingsForManipulationComparison = deepCopyCombined(combinedDataListForComparison);
		List<SSRAndPaidSeatData> pspBookingsForManipulations = deepCopyCombined(pspbc.getPspBookingsNew());

		if(overlayBaseBookingsForManipulation == null){
			overlayBaseBookingsForManipulation = pspBookingsForManipulations;
			dashStartUp = true;
		}
		
		int index = 0;
		for(CacheVO cacheVO : cacheVOContainer.getCacheImages()){

			if(dashStartUp){
				updateCacheImageWithOverallStats(cacheVO, overlayBaseBookingsForManipulation,overlayBaseBookingsForManipulationComparison);
			}else{
				overlayBaseBookingsForManipulation.add(pspBookingsForManipulations.get(index));
				updateCacheImageWithOverallStats(cacheVO, overlayBaseBookingsForManipulation,overlayBaseBookingsForManipulationComparison);
				index++;
			}
	
		}
	
	}

	private void updateCacheImageWithOverallStats(CacheVO cacheVO,
			List<SSRAndPaidSeatData> pspBaseBookingsForOverlay, List<SSRAndPaidSeatData> overlayBaseBookingsForManipulationComparison) {
		
		//boolean dashStartOrNewDay = false;
		Double sumOfRevenues = 0d;
		Integer sumOfSeats = 0;
		Integer sumOfSeatsForHour = 0;
		Integer sumOfSeatsComparison = 0;
		Double rateToAED = 1d;
		Double change = 0d;
		HashMap<String, Double> currencyMap = (HashMap<String, Double>) DashCacheUtil.getDataFromCache(DashConstants.CURRENCY_RATES_TABLE);
		//LinkedHashMap maintains insertion order
		/*
		LinkedHashMap<Integer, Integer> hourlyBreakdown = (LinkedHashMap<Integer, Integer>) DashCacheUtil.getDataFromCache(DashConstants.HOURLY_BREAKDOWN_TODAY_PAIDSEAT);
		if(hourlyBreakdown==null) {//dashStartup
			DashLoggerUtil.log(Level.INFO,"Change of Day or DashStartup");
			hourlyBreakdown = new LinkedHashMap<Integer, Integer>();
			dashStartOrNewDay = true;
		}
		*/
		
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
//		Date currentDate = new Date();
		
		//OLD
		LinkedHashMap<Integer, Integer> hourlyBreakdown = new LinkedHashMap<Integer, Integer>();

		
		
		for(SSRAndPaidSeatData pspBooking :pspBaseBookingsForOverlay){
			cal1.clear();
			cal1.setTimeInMillis(System.currentTimeMillis());
			cal2.clear();
			cal2.setTime(pspBooking.getTransDate());
			rateToAED = currencyMap.get(pspBooking.getCurrencyCode());
			if(rateToAED==null)rateToAED = 1d;
			
			sumOfSeats+= 1;//pspBooking.getTotalPax();
			sumOfRevenues+=Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
						   Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
			
			//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//			sumOfRevenues+=Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//					   Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+
//					   Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
			
			
			
//			sumOfRevenues+=(pspBooking.getAdultFare()==null?0:pspBooking.getAdultFare().doubleValue()*rateToAED) +
//			 (pspBooking.getOfwFare()==null?0:pspBooking.getOfwFare().doubleValue()*rateToAED) + (pspBooking.getChildFare()==null?0:pspBooking.getChildFare().doubleValue()*rateToAED) +
//			 (pspBooking.getTeenagerFare()==null?0:pspBooking.getTeenagerFare().doubleValue()*rateToAED);
			
//			sumOfRevenues+=(pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//			 (pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
			

			if(null!=hourlyBreakdown.get(cal2.get(Calendar.HOUR_OF_DAY))) {
				sumOfSeatsForHour = hourlyBreakdown.get(cal2.get(Calendar.HOUR_OF_DAY)).intValue() + 1;
			}
			else {
				sumOfSeatsForHour = 1;//pspBooking.getTotalPax();
			}
			hourlyBreakdown.put(cal2.get(Calendar.HOUR_OF_DAY), sumOfSeatsForHour);

			
			//New Implementation of hourlybreakdown
			//first time filling hourly data for this entire day or for current hour
			/*
			if(null!=hourlyBreakdown.get(cal2.get(Calendar.HOUR_OF_DAY))) { 
				sumOfSeatsForHour = hourlyBreakdown.get(cal2.get(Calendar.HOUR_OF_DAY)).intValue() + 1;
				//hourlyBreakdown.put(cal2.get(Calendar.HOUR_OF_DAY), sumOfSeatsForHour);
			}else { //if(null==hourlyBreakdown.get(cal2.get(Calendar.HOUR_OF_DAY))){ //first time filling data for current hour today
				sumOfSeatsForHour = 1;
				
			}
			
			hourlyBreakdown.put(cal2.get(Calendar.HOUR_OF_DAY), sumOfSeatsForHour);
			*/
		}
		
		
		for(SSRAndPaidSeatData pspBookingComparison :overlayBaseBookingsForManipulationComparison){
			cal2.clear();
			cal2.setTime(pspBookingComparison.getTransDate());
			if((cal1.get(Calendar.HOUR_OF_DAY)<cal2.get(Calendar.HOUR_OF_DAY)) ||
			   (cal1.get(Calendar.HOUR_OF_DAY)==cal2.get(Calendar.HOUR_OF_DAY) && cal1.get(Calendar.MINUTE) < cal2.get(Calendar.MINUTE)) ||
			   (cal1.get(Calendar.HOUR_OF_DAY)==cal2.get(Calendar.HOUR_OF_DAY) && cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) && cal1.get(Calendar.SECOND) <cal2.get(Calendar.SECOND)) ||
			   (cal1.get(Calendar.HOUR_OF_DAY)==cal2.get(Calendar.HOUR_OF_DAY) && cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) && cal1.get(Calendar.SECOND) ==cal2.get(Calendar.SECOND) && cal1.get(Calendar.MILLISECOND) < cal2.get(Calendar.MILLISECOND))) {
				
				//using continue because there is no guartanee that order will remain same over a few days in hashmap/list?
				continue;
			}
			
			sumOfSeatsComparison+= 1;//pspBookingComparison.getTotalPax();
			
		}
		
		if(sumOfSeatsComparison!=0) {
			change = (sumOfSeats.doubleValue() - sumOfSeatsComparison.doubleValue())/sumOfSeatsComparison.doubleValue() * 100;
			if(change>1000) {
				DashLoggerUtil.log(Level.ERROR,"Change percentage calculation too large::"+overlayBaseBookingsForManipulationComparison.size()+"::"+sumOfSeatsComparison);
			}
		}else {
			change = 0d;
			DashLoggerUtil.log(Level.ERROR,"Change percentage calculation comparison value is zero::"+overlayBaseBookingsForManipulationComparison.size()+"::"+sumOfSeatsComparison);
		}
		//removing current hour stat

//		Integer removedHour = null;
		cal1.setTimeInMillis(System.currentTimeMillis());
		
		/*OLD
		DashLoggerUtil.log(Level.INFO, "hourlybreakDown before removal" + hourlyBreakdown.toString() + "::Current Hour as per logic::" + cal1.get(Calendar.HOUR_OF_DAY));
		if(hourlyBreakdown.containsKey(cal1.get(Calendar.HOUR_OF_DAY))) {
			removedHour = hourlyBreakdown.remove(cal1.get(Calendar.HOUR_OF_DAY));
		}

		if(removedHour==null) {
			cal1.add(Calendar.HOUR_OF_DAY, -1);
			DashLoggerUtil.log(Level.ERROR, "no removal occured 1" + hourlyBreakdown.toString());
			if(hourlyBreakdown.containsKey(cal1.get(Calendar.HOUR_OF_DAY))) {
				removedHour = hourlyBreakdown.remove(cal1.get(Calendar.HOUR_OF_DAY));
			}
			if(removedHour==null) {				
				DashLoggerUtil.log(Level.ERROR, "no removal occured 2" + hourlyBreakdown.toString());
			}else {
				DashLoggerUtil.log(Level.INFO, "hourlybreakDown after removal" + hourlyBreakdown.toString() + "::Current Hour as per logic::" + cal1.get(Calendar.HOUR_OF_DAY) +
						  "current hour values removed ::" + removedHour);
			}
		}else {
			DashLoggerUtil.log(Level.INFO, "hourlybreakDown after removal" + hourlyBreakdown.toString() + "::Current Hour as per logic::" + cal1.get(Calendar.HOUR_OF_DAY) +
					  "current hour values removed ::" + removedHour);
		}
		*/
		
		
	    OverallStatsVO overallStats = new OverallStatsVO();
	    overallStats.setRevenue(sumOfRevenues);
	    overallStats.setSeats(sumOfSeats);
	    overallStats.setChange(change);
	    //ArrayList<Integer> hourlyBreakdownList = new ArrayList<Integer>(hourlyBreakdown.values());
	    
	    ArrayList<Integer> hourlyBreakdownList = addHourlyBreakdownToArrayList(hourlyBreakdown, cal1.get(Calendar.HOUR_OF_DAY));
	    DashLoggerUtil.log(Level.INFO, "hourlybreakDown is :: " + hourlyBreakdown.toString() + "::Current Hour as per logic::" + cal1.get(Calendar.HOUR_OF_DAY));
	    
	    overallStats.setHourlyBreakdown(hourlyBreakdownList);
	    
	    cacheVO.setOverallStats(overallStats);
	    
	    /*
	    if(!dashStartOrNewDay)
	    	DashCacheUtil.cleanCache(DashConstants.HOURLY_BREAKDOWN_TODAY_PAIDSEAT);
	    DashCacheUtil.setDataInCache(DashConstants.HOURLY_BREAKDOWN_TODAY_PAIDSEAT, hourlyBreakdown, System.currentTimeMillis());
	    */
	    
	    //clearing maps
	    hourlyBreakdown = null;
	    currencyMap = null;
	    cal1 = null;
	    cal2 = null;
	}

	private ArrayList<Integer> addHourlyBreakdownToArrayList(LinkedHashMap<Integer, Integer> hourlyBreakdown, int i) {
		
		ArrayList<Integer> hourlyBreakdownList = new ArrayList<Integer>();
		if(hourlyBreakdown.containsKey(23)) {
			hourlyBreakdown.remove(23);
		}
		for(Integer key : hourlyBreakdown.keySet()) {

			if(key!=i) {
				hourlyBreakdownList.add(hourlyBreakdown.get(key));
			}
			
		}		
		
		return hourlyBreakdownList;
	}

	public void initializeSeatCharacteristics(PSPBookingContainer pspbc,
			CacheVOContainer cacheVOContainer, Mode thisMode,
			Overlay thisOverlay, List<SSRAndPaidSeatData> seatSellSSRsForOverlay) {

		
		boolean dashStartUp = false;
		List<SSRAndPaidSeatData> overlayBaseBookingsSSRForManipulation = deepCopyCombined(seatSellSSRsForOverlay);
		List<SSRAndPaidSeatData> pspBookingsForManipulations = deepCopyCombined(pspbc.getPspBookingsNew());

		if(overlayBaseBookingsSSRForManipulation == null){
			overlayBaseBookingsSSRForManipulation = pspBookingsForManipulations;
			dashStartUp = true;
		}
		
		int index = 0;
		for(int i = 0; i<cacheVOContainer.getCacheImages().size(); i++){

			if(dashStartUp){
				updateCacheImageWithSeatCharacteristics(cacheVOContainer.getCacheImages().get(0), overlayBaseBookingsSSRForManipulation);
			}else{
				if(i==cacheVOContainer.getCacheImages().size()-1){ //reached the last cacheImage as per [PaidSeat_Booking_Details] data
					
					if(null!=pspBookingsForManipulations.get(index)){
						for(int j = index; j<pspBookingsForManipulations.size();j++){
							//adding all remaining SeatSellSSR data into the container for final calculation and display in the last available cacheImage
							overlayBaseBookingsSSRForManipulation.add(pspBookingsForManipulations.get(j)); 
						}
						
						updateCacheImageWithSeatCharacteristics(cacheVOContainer.getCacheImages().get(i), overlayBaseBookingsSSRForManipulation);
						index = pspBookingsForManipulations.size(); //all SSR data has now been accessed 
					}
					
					
				}else if(null!=pspBookingsForManipulations.get(index)){//DISCLAIMER : adding SeatSellSSR data to cacheImages which were split as per PSPBooking data.
					overlayBaseBookingsSSRForManipulation.add(pspBookingsForManipulations.get(index));
					updateCacheImageWithSeatCharacteristics(cacheVOContainer.getCacheImages().get(i), overlayBaseBookingsSSRForManipulation);
					index++;
				}
			}
	
		}
		
	}

	private void updateCacheImageWithSeatCharacteristics(CacheVO cacheVO,
			List<SSRAndPaidSeatData> seatSellSSRsForOverlay) {
		
		List<TopSeatCharVO> TopSeatCharList = new ArrayList<TopSeatCharVO>();
		TopSeatCharVO seatChar = null;
		int sumOfSeatsForAllCharacteristics = 0;
		Double sumOfRevenuesForAllCharacteristics = 0d;
		Map<String, Integer> mapOfSeatsPerCharacteristic = new HashMap<String, Integer>();
		Map<String, Double> mapOfRevenuesPerCharacteristic = new HashMap<String, Double>();
		int numberOfSeatsForCurrentCharacteristicTEMP = 0;
		Double revenueForCurrentCharacteristicTEMP = 0d;
		Double rateToAED = 1d;
		HashMap<String, Double> currencyMap = (HashMap<String, Double>) DashCacheUtil.getDataFromCache(DashConstants.CURRENCY_RATES_TABLE);
		
		
		for(SSRAndPaidSeatData seatSellSSR : seatSellSSRsForOverlay) {
			rateToAED = currencyMap.get(seatSellSSR.getCurrencyCode());
			if(rateToAED==null)rateToAED = 1d;
			
			if(null!=mapOfSeatsPerCharacteristic.get(seatSellSSR.getSeatCharacteristics())){
				numberOfSeatsForCurrentCharacteristicTEMP  = mapOfSeatsPerCharacteristic.get(seatSellSSR.getSeatCharacteristics()).intValue() + 1;//seatSellSSR.getTotalPax();
			}else{
				numberOfSeatsForCurrentCharacteristicTEMP  = 1;//seatSellSSR.getTotalPax();
			}
			mapOfSeatsPerCharacteristic.put(seatSellSSR.getSeatCharacteristics(),numberOfSeatsForCurrentCharacteristicTEMP);

			if(null!=mapOfRevenuesPerCharacteristic.get(seatSellSSR.getSeatCharacteristics())){
				revenueForCurrentCharacteristicTEMP = mapOfRevenuesPerCharacteristic.get(seatSellSSR.getSeatCharacteristics()).doubleValue()
													+ Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
													Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED;
				
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentCharacteristicTEMP = mapOfRevenuesPerCharacteristic.get(seatSellSSR.getSeatCharacteristics()).doubleValue()
//						+ Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
//						Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED +
//						Double.valueOf(seatSellSSR.getTax()==null?0:seatSellSSR.getTax().doubleValue()*rateToAED);
	            
//				revenueForCurrentCharacteristicTEMP = mapOfRevenuesPerCharacteristic.get(seatSellSSR.getSeatCharacteristics()).doubleValue() +
//													 (seatSellSSR.getAdultFare()==null?0:seatSellSSR.getAdultFare().doubleValue()*rateToAED) +
//													 (seatSellSSR.getOfwFare()==null?0:seatSellSSR.getOfwFare().doubleValue()*rateToAED) + (seatSellSSR.getChildFare()==null?0:seatSellSSR.getChildFare().doubleValue()*rateToAED) +
//													 (seatSellSSR.getTeenagerFare()==null?0:seatSellSSR.getTeenagerFare().doubleValue()*rateToAED);
				
//				revenueForCurrentCharacteristicTEMP = mapOfRevenuesPerCharacteristic.get(seatSellSSR.getSeatCharacteristics()).doubleValue() + 
//														(seatSellSSR.getTotal()==null?0:seatSellSSR.getTotal().doubleValue()*rateToAED)+
//														(seatSellSSR.getTotalTax()==null?0:seatSellSSR.getTotalTax().doubleValue()*rateToAED);
				
			}else{
				revenueForCurrentCharacteristicTEMP =  Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
														Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED;
				
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentCharacteristicTEMP =  Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
//						Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED+
//						Double.valueOf(seatSellSSR.getTax()==null?0:seatSellSSR.getTax().doubleValue()*rateToAED);
				
//				revenueForCurrentCharacteristicTEMP =	(seatSellSSR.getTotal()==null?0:seatSellSSR.getTotal().doubleValue()*rateToAED)+
//														(seatSellSSR.getTotalTax()==null?0:seatSellSSR.getTotalTax().doubleValue()*rateToAED);
			
//				revenueForCurrentCharacteristicTEMP = (seatSellSSR.getAdultFare()==null?0:seatSellSSR.getAdultFare().doubleValue()*rateToAED) +
//													  (seatSellSSR.getOfwFare()==null?0:seatSellSSR.getOfwFare().doubleValue()*rateToAED) + (seatSellSSR.getChildFare()==null?0:seatSellSSR.getChildFare().doubleValue()*rateToAED) +
//													  (seatSellSSR.getTeenagerFare()==null?0:seatSellSSR.getTeenagerFare().doubleValue()*rateToAED);
			}
			mapOfRevenuesPerCharacteristic.put(seatSellSSR.getSeatCharacteristics(), revenueForCurrentCharacteristicTEMP);
				
			sumOfSeatsForAllCharacteristics+= 1;//seatSellSSR.getTotalPax();
			sumOfRevenuesForAllCharacteristics+=Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
				      					 Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED;
			
			//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//			sumOfRevenuesForAllCharacteristics+=Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
// 					 Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED+
// 					Double.valueOf(seatSellSSR.getTax()==null?0:seatSellSSR.getTax().doubleValue()*rateToAED);
			
//			sumOfRevenuesForAllCharacteristics+=(seatSellSSR.getTotal()==null?0:seatSellSSR.getTotal().doubleValue()*rateToAED)+
//												(seatSellSSR.getTotalTax()==null?0:seatSellSSR.getTotalTax().doubleValue()*rateToAED);
			
//			sumOfRevenuesForAllCharacteristics+=(seatSellSSR.getAdultFare()==null?0:seatSellSSR.getAdultFare().doubleValue()*rateToAED) +
//												(seatSellSSR.getOfwFare()==null?0:seatSellSSR.getOfwFare().doubleValue()*rateToAED) + (seatSellSSR.getChildFare()==null?0:seatSellSSR.getChildFare().doubleValue()*rateToAED) +
//												(seatSellSSR.getTeenagerFare()==null?0:seatSellSSR.getTeenagerFare().doubleValue()*rateToAED);
		}
		
		mapOfRevenuesPerCharacteristic = MapSortUtil.sortByValueDescending(mapOfRevenuesPerCharacteristic);
		mapOfSeatsPerCharacteristic = MapSortUtil.sortByValueDescending(mapOfSeatsPerCharacteristic);
		//Ordering for UI is only by seats
		List<Map.Entry<String, Integer>> listPSPSeatsByCharacteristic = new LinkedList<Map.Entry<String, Integer>>(mapOfSeatsPerCharacteristic.entrySet());
		
		//top 4 only to be displayed
		if(null!=listPSPSeatsByCharacteristic) {
			for(int i = 0; i<listPSPSeatsByCharacteristic.size(); i++){
				if(i<listPSPSeatsByCharacteristic.size()){
				     if(null!=listPSPSeatsByCharacteristic.get(i)){
				    	seatChar = new TopSeatCharVO();
						seatChar.setSeatCharacteristic(DashProperties.getProperty(DashConstants.SEATCHAR+listPSPSeatsByCharacteristic.get(i).getKey()));
			
						seatChar.setSeatsPercentage(mapOfSeatsPerCharacteristic.get(listPSPSeatsByCharacteristic.get(i).getKey()).doubleValue() / 
								sumOfSeatsForAllCharacteristics * 100);
						seatChar.setSeats(mapOfSeatsPerCharacteristic.get(listPSPSeatsByCharacteristic.get(i).getKey()));
						
						seatChar.setRevenue(mapOfRevenuesPerCharacteristic.get(listPSPSeatsByCharacteristic.get(i).getKey()));
						seatChar.setRevenuePercentage(mapOfRevenuesPerCharacteristic.get(listPSPSeatsByCharacteristic.get(i).getKey()).doubleValue() /
								sumOfRevenuesForAllCharacteristics * 100);
						TopSeatCharList.add(seatChar);
					}
				}
			}
		}
		
		sortSeatCharForUIRequirement(TopSeatCharList);
		cacheVO.setTopSeatCharacteristics(TopSeatCharList);
		
		
		//clearing maps 
		listPSPSeatsByCharacteristic = null;
		currencyMap = null;
		mapOfSeatsPerCharacteristic = null;
		mapOfRevenuesPerCharacteristic = null;
	}


	private void sortSeatCharForUIRequirement(List<TopSeatCharVO> TopSeatCharList) {
		
		/*
		 * 	Regular
		•	Preferred (Lower)
		•	Preferred (Upper)
			Exit 
		•	Twin (Lower)
		•	Twin (Upper)
		•	
		*/
		
		
		boolean isExitPresent = false;
		boolean isPreferredLowerPresent = false;
		boolean isPreferredUpperPresent = false;
		boolean isTwinLowerPresent = false;
		boolean isTwinUpperPresent = false;
		boolean isRegularPresent = false;
		TopSeatCharVO topSeatCharVO = null;
		
		for(int i = 0; i <TopSeatCharList.size() ; i++) {
			
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"E").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				isExitPresent = true;
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"O").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				isPreferredLowerPresent = true;
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"CC").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				isPreferredUpperPresent = true;
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"T").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				isTwinLowerPresent = true;
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"WA").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				isTwinUpperPresent = true;
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"1").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				isRegularPresent = true;
			}
			
			
		}
		if(!isExitPresent){
			topSeatCharVO = new TopSeatCharVO();
			topSeatCharVO.setSeatCharacteristic(DashProperties.getProperty(DashConstants.SEATCHAR+"E"));
			topSeatCharVO.setRevenue(0d);
			topSeatCharVO.setRevenuePercentage(0d);
			topSeatCharVO.setSeats(0);
			topSeatCharVO.setSeatsPercentage(0d);
			
			TopSeatCharList.add(topSeatCharVO);
		}
		if(!isPreferredLowerPresent){
			
			topSeatCharVO = new TopSeatCharVO();
			topSeatCharVO.setSeatCharacteristic(DashProperties.getProperty(DashConstants.SEATCHAR+"O"));
			topSeatCharVO.setRevenue(0d);
			topSeatCharVO.setRevenuePercentage(0d);
			topSeatCharVO.setSeats(0);
			topSeatCharVO.setSeatsPercentage(0d);
			TopSeatCharList.add(topSeatCharVO);
		}
		if(!isPreferredUpperPresent){
			
			topSeatCharVO = new TopSeatCharVO();
			topSeatCharVO.setSeatCharacteristic(DashProperties.getProperty(DashConstants.SEATCHAR+"CC"));
			topSeatCharVO.setRevenue(0d);
			topSeatCharVO.setRevenuePercentage(0d);
			topSeatCharVO.setSeats(0);
			topSeatCharVO.setSeatsPercentage(0d);
			TopSeatCharList.add(topSeatCharVO);
		}
		if(!isTwinLowerPresent){
			
			topSeatCharVO = new TopSeatCharVO();
			topSeatCharVO.setSeatCharacteristic(DashProperties.getProperty(DashConstants.SEATCHAR+"T"));
			topSeatCharVO.setRevenue(0d);
			topSeatCharVO.setRevenuePercentage(0d);
			topSeatCharVO.setSeats(0);
			topSeatCharVO.setSeatsPercentage(0d);
			TopSeatCharList.add(topSeatCharVO);
		}
		if(!isTwinUpperPresent){
			
			topSeatCharVO = new TopSeatCharVO();
			topSeatCharVO.setSeatCharacteristic(DashProperties.getProperty(DashConstants.SEATCHAR+"WA"));
			topSeatCharVO.setRevenue(0d);
			topSeatCharVO.setRevenuePercentage(0d);
			topSeatCharVO.setSeats(0);
			topSeatCharVO.setSeatsPercentage(0d);
			TopSeatCharList.add(topSeatCharVO);
		}
		if(!isRegularPresent){
			
			topSeatCharVO = new TopSeatCharVO();
			topSeatCharVO.setSeatCharacteristic(DashProperties.getProperty(DashConstants.SEATCHAR+"1"));
			topSeatCharVO.setRevenue(0d);
			topSeatCharVO.setRevenuePercentage(0d);
			topSeatCharVO.setSeats(0);
			topSeatCharVO.setSeatsPercentage(0d);
			TopSeatCharList.add(topSeatCharVO);
		}
		
		for(int i = 0; i <TopSeatCharList.size() ; i++) {
			
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"1").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				
				Collections.swap(TopSeatCharList, i, 0);
				TopSeatCharList.get(0).setSeatCharacteristic("Regular Seat");
			}
			
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"O").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				
				Collections.swap(TopSeatCharList, i, 1);
				TopSeatCharList.get(1).setSeatCharacteristic("Preferred Lower");
		    	
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"E").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				
				Collections.swap(TopSeatCharList, i, 2);
				TopSeatCharList.get(2).setSeatCharacteristic("Exit Row");
		    	
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"T").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				
				Collections.swap(TopSeatCharList, i, 3);
				TopSeatCharList.get(3).setSeatCharacteristic("Twin Lower");
		    	
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"WA").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				
				Collections.swap(TopSeatCharList, i, 4);
				TopSeatCharList.get(4).setSeatCharacteristic("Twin Upper");
		    	
			}
			if(DashProperties.getProperty(DashConstants.SEATCHAR+"CC").equalsIgnoreCase(TopSeatCharList.get(i).getSeatCharacteristic())) {
				
				Collections.swap(TopSeatCharList, i, 5);
				TopSeatCharList.get(5).setSeatCharacteristic("Preferred Upper");
		    	
			}

		}
		
		if(TopSeatCharList.size()>6) {
			for(int j=6; j<TopSeatCharList.size();j++) {
				if(TopSeatCharList.get(j)!=null)
					TopSeatCharList.remove(j);
			}
		}
			
	}


	

	

	public ArrayList<MonthlyVO> initializeMonthlyBreakdown(PSPBookingContainer pspbc,
			CacheVOContainer cacheVOContainer, Mode thisMode,
			Overlay thisOverlay, ArrayList<MonthlyVO> monthlyBreakDown) {
		
		boolean dashStart = false;
		
		
		if(monthlyBreakDown==null){
			dashStart = true;
			
		}
		int index = 0;
		for(CacheVO cacheVO : cacheVOContainer.getCacheImages()){

			if(dashStart){
				monthlyBreakDown = updateCacheImageWithMonthlyBreakdown(cacheVO, pspbc.getPspBookingsForTheYearNew(), null, null);
			}else{
				monthlyBreakDown = updateCacheImageWithMonthlyBreakdown(cacheVO, null, monthlyBreakDown, pspbc.getPspBookingsNew().get(index));
				index++;
			}
	
		}
		
		return monthlyBreakDown;		
	}

	private ArrayList<MonthlyVO> updateCacheImageWithMonthlyBreakdown(CacheVO cacheVO,
			List<SSRAndPaidSeatData> pspBookingsForTheYear, ArrayList<MonthlyVO> monthlyBreakDown, SSRAndPaidSeatData currentPSPBooking) {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM-yy");
		Map<String, Integer> breakdownMap = new HashMap<String, Integer>();
		boolean inputProcessedFlag = false;
		MonthlyVO newMonth = null;
		Integer totalSeatsOverTheYear = 0;
		
		//if dashStartup
		if(monthlyBreakDown == null){
			
			for(SSRAndPaidSeatData pspBooking : pspBookingsForTheYear){
				
				cal.setTime(pspBooking.getTransDate());
				breakdownMap.put(sdf.format(cal.getTime()), breakdownMap.get(sdf.format(cal.getTime()))==null?1: 
						             breakdownMap.get(sdf.format(cal.getTime())) +1); 
				
				totalSeatsOverTheYear++;
				
				
			}
			
			monthlyBreakDown = new ArrayList<MonthlyVO>();
			for(String monthYear : breakdownMap.keySet()){
				newMonth = new MonthlyVO();
				newMonth.setMonthYear(monthYear);
				newMonth.setSeats(breakdownMap.get(monthYear));
				monthlyBreakDown.add(newMonth);
				
			}
		}else{//for routine table updates


				cal.setTime(currentPSPBooking.getTransDate());
				for(MonthlyVO monthlyVO : monthlyBreakDown){
					if(monthlyVO.getMonthYear().equalsIgnoreCase(sdf.format(cal.getTime()))){
						monthlyVO.setSeats(monthlyVO.getSeats()==null?1:monthlyVO.getSeats() + 1);
						inputProcessedFlag = true;
					}
					totalSeatsOverTheYear+=monthlyVO.getSeats();
					
				}
				
				if(!inputProcessedFlag){//new month has arrived
					newMonth = new MonthlyVO();
					newMonth.setMonthYear(sdf.format(cal.getTime()));
					newMonth.setSeats(1);
					monthlyBreakDown.add(newMonth);
					totalSeatsOverTheYear++;
				}
							
		}
		
		Collections.sort(monthlyBreakDown, new Comparator<MonthlyVO>() {
		    @Override
		    public int compare(MonthlyVO a, MonthlyVO b) {
		        return b.getSeats().compareTo(a.getSeats());
		    }
		});
		
		if(monthlyBreakDown.size()>12){ //downsizing to 12 months for display
			for(int i = monthlyBreakDown.size() - 1; i>11; i--){
				
				monthlyBreakDown.remove(i);
			}
		}
		
		//setting seats percentage 
		if(totalSeatsOverTheYear!=0)
		for(MonthlyVO monthlyVO : monthlyBreakDown) {
			monthlyVO.setSeatPercentageOverYear(Double.valueOf(String.valueOf(monthlyVO.getSeats()/totalSeatsOverTheYear * 100)));
			
		}
		
		ArrayList<MonthlyVO> temp = deepCopyMonthly(monthlyBreakDown);
		cacheVO.setMonthlySplit(temp);
		return monthlyBreakDown;
		
	}

	public void initializeHaulSplit(PSPBookingContainer pspbc,
			CacheVOContainer cacheVOContainer, Mode thisMode,
			Overlay thisOverlay, List<SSRAndPaidSeatData> pspBaseBookingsForOverlay) {
		

		boolean dashStartUp = false;
		List<SSRAndPaidSeatData> overlayBaseBookingsForManipulation = deepCopyCombined(pspBaseBookingsForOverlay);
		List<SSRAndPaidSeatData> pspBookingsForManipulations = deepCopyCombined(pspbc.getPspBookingsNew());

		if(overlayBaseBookingsForManipulation == null){
			overlayBaseBookingsForManipulation = pspBookingsForManipulations;
			dashStartUp = true;
		}
		
		int index = 0;
		for(CacheVO cacheVO : cacheVOContainer.getCacheImages()){

			if(dashStartUp){
				updateCacheImageWithHaulTypes(cacheVO, overlayBaseBookingsForManipulation);
			}else{
				overlayBaseBookingsForManipulation.add(pspBookingsForManipulations.get(index));
				updateCacheImageWithHaulTypes(cacheVO, overlayBaseBookingsForManipulation);
				index++;
			}
	
		}
		
		
	}
	

	private void updateCacheImageWithHaulTypes(CacheVO cacheVO,
			List<SSRAndPaidSeatData> pspBaseBookingsForOverlay) {
		// TODO Auto-generated method stub
		
		HashMap<String,HaulType> haulTypesForOND  = (HashMap<String, HaulType>)DashCacheUtil.getDataFromCache(DashConstants.HAUL_TYPES_TABLE);
		if(null == haulTypesForOND || haulTypesForOND.size()==0){
			return;
		}
		MasterResponseDTO locationsMapContainer = (MasterResponseDTO)DashCacheUtil.getDataFromCache(DashConstants.LOCATIONS_CACHE_KEY);
		
		String currentHaul = "";
		List<TopHaulVO> topHaulBreakup = new ArrayList<TopHaulVO>();
		TopHaulVO haulVO = null;
		int sumOfSeatsForAllHauls = 0;
		Double sumOfRevenuesForAllHauls = 0d;
		Map<String, Integer> mapOfSeatsPerHaulType = new HashMap<String, Integer>();
		Map<String, Double> mapOfRevenuesPerHaulType = new HashMap<String, Double>();
		int numberOfSeatsForCurrentHaulTEMP = 0;
		Double revenueForCurrentHaulTEMP = 0d;
		Double rateToAED = 1d;
		HashMap<String, Double> currencyMap = (HashMap<String, Double>) DashCacheUtil.getDataFromCache(DashConstants.CURRENCY_RATES_TABLE);

		for(SSRAndPaidSeatData pspBooking : pspBaseBookingsForOverlay) {
			rateToAED = currencyMap.get(pspBooking.getCurrencyCode());
			if(rateToAED==null)rateToAED = 1d;
			
			currentHaul = retrieveHaul(haulTypesForOND, pspBooking.getOrigin(), pspBooking.getDestination(),locationsMapContainer);
			
			if(currentHaul.equalsIgnoreCase("")) {
				currentHaul = DashConstants.MEDIUM;
			}
			
			if(null!=mapOfSeatsPerHaulType.get(currentHaul)){
				numberOfSeatsForCurrentHaulTEMP  = mapOfSeatsPerHaulType.get(currentHaul).intValue() + 1;// pspBooking.getTotalPax();
			}else{
				numberOfSeatsForCurrentHaulTEMP  = 1;//pspBooking.getTotalPax();
			}
			mapOfSeatsPerHaulType.put(currentHaul,numberOfSeatsForCurrentHaulTEMP);

			if(null!=mapOfRevenuesPerHaulType.get(currentHaul)){
//				revenueForCurrentHaulTEMP = mapOfRevenuesPerHaulType.get(currentHaul).doubleValue() + (pspBooking.getAdultFare()==null?0:pspBooking.getAdultFare().doubleValue()*rateToAED) +
//	                    						(pspBooking.getOfwFare()==null?0:pspBooking.getOfwFare().doubleValue()*rateToAED) + (pspBooking.getChildFare()==null?0:pspBooking.getChildFare().doubleValue()*rateToAED) +
//	                    						(pspBooking.getTeenagerFare()==null?0:pspBooking.getTeenagerFare().doubleValue()*rateToAED);
				revenueForCurrentHaulTEMP = mapOfRevenuesPerHaulType.get(currentHaul).doubleValue() + 
											Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
											Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentHaulTEMP = mapOfRevenuesPerHaulType.get(currentHaul).doubleValue() + 
//						Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//						Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+		
//						Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
				
//				revenueForCurrentHaulTEMP = mapOfRevenuesPerHaulType.get(currentHaul).doubleValue() + (pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//											(pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
			}else{
//				revenueForCurrentHaulTEMP = (pspBooking.getAdultFare()==null?0:pspBooking.getAdultFare().doubleValue()*rateToAED) +
//						(pspBooking.getOfwFare()==null?0:pspBooking.getOfwFare().doubleValue()*rateToAED) + (pspBooking.getChildFare()==null?0:pspBooking.getChildFare().doubleValue()*rateToAED) +
//						(pspBooking.getTeenagerFare()==null?0:pspBooking.getTeenagerFare().doubleValue()*rateToAED);
				
				revenueForCurrentHaulTEMP = Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
				Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
				
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentHaulTEMP = Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//											Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+
//											Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
				
//				revenueForCurrentHaulTEMP = (pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//											(pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
			}
			mapOfRevenuesPerHaulType.put(currentHaul, revenueForCurrentHaulTEMP);
				
			sumOfSeatsForAllHauls+=1;//pspBooking.getTotalPax();
//			sumOfRevenuesForAllHauls+=(pspBooking.getAdultFare()==null?0:pspBooking.getAdultFare().doubleValue()*rateToAED) +
//                    (pspBooking.getOfwFare()==null?0:pspBooking.getOfwFare().doubleValue()*rateToAED) + (pspBooking.getChildFare()==null?0:pspBooking.getChildFare().doubleValue()*rateToAED) +
//                    (pspBooking.getTeenagerFare()==null?0:pspBooking.getTeenagerFare().doubleValue()*rateToAED);
			
			sumOfRevenuesForAllHauls+=Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
									  Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
			
			//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//			sumOfRevenuesForAllHauls+=Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//					  Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+
//					  Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
			
			
//			sumOfRevenuesForAllHauls+=	(pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//										(pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
			
			
		}
		
		mapOfRevenuesPerHaulType = MapSortUtil.sortByValueDescending(mapOfRevenuesPerHaulType);
		mapOfSeatsPerHaulType = MapSortUtil.sortByValueDescending(mapOfSeatsPerHaulType);
		//Ordering for UI is only by seats
		List<Map.Entry<String, Integer>> listPSPSeatsByHaulType = new LinkedList<Map.Entry<String, Integer>>(mapOfSeatsPerHaulType.entrySet());
		

		for(int i = 0; i<listPSPSeatsByHaulType.size(); i++){
			if(null!=listPSPSeatsByHaulType && null!=listPSPSeatsByHaulType.get(i)){
				haulVO = new TopHaulVO();
				switch(listPSPSeatsByHaulType.get(i).getKey()){
				
					case DashConstants.SHORT : haulVO.setHaulType(DashConstants.SHORT_HAUL);break;
					case DashConstants.MEDIUM : haulVO.setHaulType(DashConstants.MEDIUM_HAUL);break;
					case DashConstants.LONG : haulVO.setHaulType(DashConstants.LONG_HAUL);break;
					case DashConstants.UNKNOWN_HAUL : haulVO.setHaulType(DashConstants.MEDIUM_HAUL);break;
					default : break;
				
				}
				haulVO.setBookingsPercentage(mapOfSeatsPerHaulType.get(listPSPSeatsByHaulType.get(i).getKey()).doubleValue() / 
						sumOfSeatsForAllHauls * 100);
				haulVO.setBookings(mapOfSeatsPerHaulType.get(listPSPSeatsByHaulType.get(i).getKey()));
				
				haulVO.setRevenue(mapOfRevenuesPerHaulType.get(listPSPSeatsByHaulType.get(i).getKey()));
				haulVO.setRevenuePercentage(mapOfRevenuesPerHaulType.get(listPSPSeatsByHaulType.get(i).getKey()).doubleValue() /
						sumOfRevenuesForAllHauls * 100);
				topHaulBreakup.add(haulVO);
			}
		}
			

		sortHaulsForUIRequirement(topHaulBreakup);
		cacheVO.setTopHaultypes(topHaulBreakup);
		
		
		//clearing maps 
		listPSPSeatsByHaulType = null;
		currencyMap = null;
		mapOfSeatsPerHaulType = null;
		mapOfRevenuesPerHaulType = null;
		
	}
	private void sortHaulsForUIRequirement(List<TopHaulVO> topHaulBreakup ) {
		
		TopHaulVO topHaulVO = null;
		boolean isShortPresent = false;
		boolean isMediumPresent = false;
		boolean isLongPresent = false;
		for(int i = 0; i <topHaulBreakup.size() ; i++) {
			
			if(DashConstants.SHORT_HAUL.equalsIgnoreCase(topHaulBreakup.get(i).getHaulType())) {
				isShortPresent = true;
			}

			if(DashConstants.MEDIUM_HAUL.equalsIgnoreCase(topHaulBreakup.get(i).getHaulType())) {
				
				isMediumPresent = true;
		    	
			}
			if(DashConstants.LONG_HAUL.equalsIgnoreCase(topHaulBreakup.get(i).getHaulType())) {

				isLongPresent = true;
			}
			
		}
		
		if(!isShortPresent) {
			topHaulVO = new TopHaulVO();
			topHaulVO.setHaulType(DashConstants.SHORT_HAUL);
			topHaulVO.setBookings(0);
			topHaulVO.setBookingsPercentage(0d);
			topHaulVO.setRevenue(0d);
			topHaulVO.setRevenuePercentage(0d);
			topHaulBreakup.add(topHaulVO);
		}
		if(!isMediumPresent) {
			
			topHaulVO = new TopHaulVO();
			topHaulVO.setHaulType(DashConstants.MEDIUM_HAUL);
			topHaulVO.setBookings(0);
			topHaulVO.setBookingsPercentage(0d);
			topHaulVO.setRevenue(0d);
			topHaulVO.setRevenuePercentage(0d);
			topHaulBreakup.add(topHaulVO);
		}
		if(!isLongPresent) {
			
			topHaulVO = new TopHaulVO();
			topHaulVO.setHaulType(DashConstants.LONG_HAUL);
			topHaulVO.setBookings(0);
			topHaulVO.setBookingsPercentage(0d);
			topHaulVO.setRevenue(0d);
			topHaulVO.setRevenuePercentage(0d);
			topHaulBreakup.add(topHaulVO);
		}
		
		
		for(int i = 0; i <topHaulBreakup.size() ; i++) {
			
			if(DashConstants.SHORT_HAUL.equalsIgnoreCase(topHaulBreakup.get(i).getHaulType())) {
				
				Collections.swap(topHaulBreakup, i, 0); 	
			}
			
			if(DashConstants.MEDIUM_HAUL.equalsIgnoreCase(topHaulBreakup.get(i).getHaulType())) {
				
				Collections.swap(topHaulBreakup, i, 1);
		    	
			}
			if(DashConstants.LONG_HAUL.equalsIgnoreCase(topHaulBreakup.get(i).getHaulType())) {
				
				Collections.swap(topHaulBreakup, i, 2);
		    	
			}

		}
			
	}

	private String retrieveHaul(HashMap<String,HaulType> haulTypesForOND, String origin,
			String destination, MasterResponseDTO locationsMapContainer) {
		String haulKey = "";
		try {
		 haulKey = locationsMapContainer.getEKLocations().get(origin).getCountry()+"-"+locationsMapContainer.getEKLocations().get(destination).getCountry();
		 return haulTypesForOND.get(haulKey).getHaul();
		}catch(Exception e) {
			HaulType tempHaul = null ;
			for (Map.Entry<String, HaulType> entry : haulTypesForOND.entrySet())
			{	
				try {
					tempHaul = entry.getValue();
				    if((locationsMapContainer.getEKLocations().get(origin).getCountry().contains(tempHaul.getOrigin()) ||
				       tempHaul.getOrigin().contains(locationsMapContainer.getEKLocations().get(origin).getCountry())) 
				       && 
				    (locationsMapContainer.getEKLocations().get(destination).getCountry().contains(tempHaul.getDestination()) || 
				    		tempHaul.getDestination().contains(locationsMapContainer.getEKLocations().get(destination).getCountry())))	 {
				    	
				    	return tempHaul.getHaul();
				    	
				    }
				}catch(Exception e2) {
					DashLoggerUtil.log(Level.DEBUG, "Location xml and Haul table mismatch for for ::"+ origin + "destination::" + destination);
					return "";

				}

			}
			DashLoggerUtil.log(Level.DEBUG, "Location xml and Haul table mismatch for for ::"+ origin + "destination::" + destination);
			return "";

		}
		
		
	}

	public void initializeBrandSplit(PSPBookingContainer pspbc,
			CacheVOContainer cacheVOContainer, Mode thisMode,
			Overlay thisOverlay, List<SSRAndPaidSeatData> seatSellSSRsForOverlay) {

		boolean dashStartUp = false;
		
		List<SSRAndPaidSeatData> overlayBaseBookingsSSRForManipulation = deepCopyCombined(seatSellSSRsForOverlay);
		List<SSRAndPaidSeatData> pspBookingsForManipulations = deepCopyCombined(pspbc.getPspBookingsNew());
		
		if(overlayBaseBookingsSSRForManipulation == null){
			overlayBaseBookingsSSRForManipulation = pspBookingsForManipulations;
			dashStartUp = true;
		}
		
		int index = 0;		
		for(int i = 0; i<cacheVOContainer.getCacheImages().size(); i++){

			if(dashStartUp){
				updateCacheImageWithBrandSplit(cacheVOContainer.getCacheImages().get(0), overlayBaseBookingsSSRForManipulation);
			}else{
				if(i==cacheVOContainer.getCacheImages().size()-1){ //reached the last cacheImage as per [PaidSeat_Booking_Details] data
					
					if(null!=pspBookingsForManipulations.get(index)){
						for(int j = index; j<pspBookingsForManipulations.size();j++){
							//adding all remaining SeatSellSSR data into the container for final calculation and display in the last available cacheImage
							overlayBaseBookingsSSRForManipulation.add(pspBookingsForManipulations.get(j)); 
						}
						
						updateCacheImageWithBrandSplit(cacheVOContainer.getCacheImages().get(i), overlayBaseBookingsSSRForManipulation);
						index = pspBookingsForManipulations.size(); //all SSR data has now been accessed 
					}
					
					
				}else if(null!=pspBookingsForManipulations.get(index)){//DISCLAIMER : adding SeatSellSSR data to cacheImages which were split as per PSPBooking data.
					overlayBaseBookingsSSRForManipulation.add(pspBookingsForManipulations.get(index));
					updateCacheImageWithBrandSplit(cacheVOContainer.getCacheImages().get(i), overlayBaseBookingsSSRForManipulation);
					index++;
				}
			}
	
		}
		
		
		
		
	}

	private void updateCacheImageWithBrandSplit(CacheVO cacheVO,
			List<SSRAndPaidSeatData> seatSellSSRsForOverlay) {
	
		Double rateToAED = 1d;
		HashMap<String, Double> currencyMap = (HashMap<String, Double>) DashCacheUtil.getDataFromCache(DashConstants.CURRENCY_RATES_TABLE);
		
		List<TopBrandsVO> TopBrandsList = new ArrayList<TopBrandsVO>();
		TopBrandsVO brandSplit = null;
		int sumOfSeatsForAllBrands = 0;
		Double sumOfRevenuesForAllBrands = 0d;
		Map<String, Integer> mapOfSeatsPerBrand = new HashMap<String, Integer>();
		Map<String, Double> mapOfRevenuesPerBrand = new HashMap<String, Double>();
		int numberOfSeatsForCurrentBrandTEMP = 0;
		Double revenueForCurrentBrandTEMP = 0d;
		String currentDisplayableBrand = "";
		
		
		for(SSRAndPaidSeatData seatSellSSR : seatSellSSRsForOverlay) {
			
			rateToAED = currencyMap.get(seatSellSSR.getCurrencyCode());
			if(rateToAED==null)rateToAED = 1d;
			
			if(DashProperties.getProperty("special").contains(seatSellSSR.getCabinClass())){
				currentDisplayableBrand = "Special";
			}
			if(DashProperties.getProperty("saver").contains(seatSellSSR.getCabinClass())){
				currentDisplayableBrand = "Saver";			
			}
			if(DashProperties.getProperty("flex").contains(seatSellSSR.getCabinClass())){
				currentDisplayableBrand = "Flex";
			}
			if(DashProperties.getProperty("plus").contains(seatSellSSR.getCabinClass())){
				currentDisplayableBrand = "Flex Plus";
			}
			if(DashProperties.getProperty("group").contains(seatSellSSR.getCabinClass())){
				currentDisplayableBrand = "Group";
			}
			
			
			if(null!=mapOfSeatsPerBrand.get(currentDisplayableBrand)){
				numberOfSeatsForCurrentBrandTEMP  = mapOfSeatsPerBrand.get(currentDisplayableBrand).intValue() + 1;//seatSellSSR.getTotalPax();
			}else{
				numberOfSeatsForCurrentBrandTEMP  = 1;//seatSellSSR.getTotalPax();
			}
			mapOfSeatsPerBrand.put(currentDisplayableBrand,numberOfSeatsForCurrentBrandTEMP);

			if(null!=mapOfRevenuesPerBrand.get(currentDisplayableBrand)){
				revenueForCurrentBrandTEMP = mapOfRevenuesPerBrand.get(currentDisplayableBrand).doubleValue()
													+ Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
													Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED;
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentBrandTEMP = mapOfRevenuesPerBrand.get(currentDisplayableBrand).doubleValue()
//						+ Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
//						Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED+
//						Double.valueOf(seatSellSSR.getTax()==null?0:seatSellSSR.getTax().doubleValue()*rateToAED);
				
//				revenueForCurrentBrandTEMP = mapOfRevenuesPerBrand.get(currentDisplayableBrand).doubleValue() + (seatSellSSR.getTotal()==null?0:seatSellSSR.getTotal().doubleValue()*rateToAED)+
//											(seatSellSSR.getTotalTax()==null?0:seatSellSSR.getTotalTax().doubleValue()*rateToAED);
				
//				revenueForCurrentBrandTEMP = mapOfRevenuesPerBrand.get(currentDisplayableBrand).doubleValue() +
//											(seatSellSSR.getAdultFare()==null?0:seatSellSSR.getAdultFare().doubleValue()*rateToAED) +
//											(seatSellSSR.getOfwFare()==null?0:seatSellSSR.getOfwFare().doubleValue()*rateToAED) + (seatSellSSR.getChildFare()==null?0:seatSellSSR.getChildFare().doubleValue()*rateToAED) +
//											(seatSellSSR.getTeenagerFare()==null?0:seatSellSSR.getTeenagerFare().doubleValue()*rateToAED);
						 
			}else{
				
//				revenueForCurrentBrandTEMP =	(seatSellSSR.getTotal()==null?0:seatSellSSR.getTotal().doubleValue()*rateToAED)+
//												(seatSellSSR.getTotalTax()==null?0:seatSellSSR.getTotalTax().doubleValue()*rateToAED);
				
				revenueForCurrentBrandTEMP = Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
												      Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED;
												      
				
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentBrandTEMP = Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
//											 Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED+
//											 Double.valueOf(seatSellSSR.getTax()==null?0:seatSellSSR.getTax().doubleValue()*rateToAED);

//				revenueForCurrentBrandTEMP = (seatSellSSR.getAdultFare()==null?0:seatSellSSR.getAdultFare().doubleValue()*rateToAED) +
//											 (seatSellSSR.getOfwFare()==null?0:seatSellSSR.getOfwFare().doubleValue()*rateToAED) + (seatSellSSR.getChildFare()==null?0:seatSellSSR.getChildFare().doubleValue()*rateToAED) +
//											 (seatSellSSR.getTeenagerFare()==null?0:seatSellSSR.getTeenagerFare().doubleValue()*rateToAED);
			}
			mapOfRevenuesPerBrand.put(currentDisplayableBrand, revenueForCurrentBrandTEMP);
				
			sumOfSeatsForAllBrands+= 1;//seatSellSSR.getTotalPax();
//			sumOfRevenuesForAllBrands+= (seatSellSSR.getTotal()==null?0:seatSellSSR.getTotal().doubleValue()*rateToAED)+
//										(seatSellSSR.getTotalTax()==null?0:seatSellSSR.getTotalTax().doubleValue()*rateToAED);
			sumOfRevenuesForAllBrands+=Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
				      					 Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED;
			
			//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//			sumOfRevenuesForAllBrands+=Double.valueOf(seatSellSSR.getBaseFare()==null||seatSellSSR.getBaseFare().equalsIgnoreCase("")?"0":seatSellSSR.getBaseFare())*rateToAED +
// 					 Double.valueOf(seatSellSSR.getExchangedFare()==null||seatSellSSR.getExchangedFare().equalsIgnoreCase("")?"0":seatSellSSR.getExchangedFare())*rateToAED+
// 					 Double.valueOf(seatSellSSR.getTax()==null?0:seatSellSSR.getTax().doubleValue()*rateToAED);
			
//			sumOfRevenuesForAllBrands+=(seatSellSSR.getAdultFare()==null?0:seatSellSSR.getAdultFare().doubleValue()*rateToAED) +
//                    (seatSellSSR.getOfwFare()==null?0:seatSellSSR.getOfwFare().doubleValue()*rateToAED) + (seatSellSSR.getChildFare()==null?0:seatSellSSR.getChildFare().doubleValue()*rateToAED) +
//                    (seatSellSSR.getTeenagerFare()==null?0:seatSellSSR.getTeenagerFare().doubleValue()*rateToAED);
			
		}
		
		mapOfRevenuesPerBrand = MapSortUtil.sortByValueDescending(mapOfRevenuesPerBrand);
		mapOfSeatsPerBrand = MapSortUtil.sortByValueDescending(mapOfSeatsPerBrand);
		//Ordering for UI is only by seats
		List<Map.Entry<String, Integer>> listPSPSeatsByBrand = new LinkedList<Map.Entry<String, Integer>>(mapOfSeatsPerBrand.entrySet());
		
		//top 4 only to be displayed
		//CHANGED

		for(int i = 0; i<5; i++){
			if(null!=listPSPSeatsByBrand && i<listPSPSeatsByBrand.size()){
				if(null!=listPSPSeatsByBrand.get(i)){
					brandSplit = new TopBrandsVO();
					brandSplit.setFareBrand(listPSPSeatsByBrand.get(i).getKey());
		
					brandSplit.setBookingsPercentage(mapOfSeatsPerBrand.get(listPSPSeatsByBrand.get(i).getKey()).doubleValue() / 
							sumOfSeatsForAllBrands * 100);
					brandSplit.setBookings(mapOfSeatsPerBrand.get(listPSPSeatsByBrand.get(i).getKey()));
					
					brandSplit.setRevenue(mapOfRevenuesPerBrand.get(listPSPSeatsByBrand.get(i).getKey()));
					brandSplit.setRevenuePercentage(mapOfRevenuesPerBrand.get(listPSPSeatsByBrand.get(i).getKey()).doubleValue() /
							sumOfRevenuesForAllBrands * 100);
					TopBrandsList.add(brandSplit);
				}
			}

		}

		
		
		sortBrandsForUIRequirement(TopBrandsList);
		cacheVO.setTopFareBrands(TopBrandsList);	
		
		
		//clearing maps 
		listPSPSeatsByBrand = null;
		currencyMap = null;
		mapOfSeatsPerBrand = null;
		mapOfRevenuesPerBrand = null;
		
		
	}
	
	
	
	  private void sortBrandsForUIRequirement(List<TopBrandsVO> TopBrandsList) {
		
		TopBrandsVO brandsVO = null;
		boolean isSpecialPresent = false;
		boolean isSaverPresent =false;
		boolean isFlexPresent = false;
		boolean isFlexPlusPresent = false;
		boolean isGroupPresent = false;
		
		 for(int i = 0; i <TopBrandsList.size() ; i++) {
				
			 if("Special".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
					
				 isSpecialPresent = true;
			 }
				
			 if("Saver".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
					
				 isSaverPresent= true;
			    	
			 }
			 if("Flex".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
					
				 isFlexPresent= true;
			    	
			 }
			 if("Flex Plus".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
					
				 isFlexPlusPresent= true;
			    	
			 }
			 if("Group".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
					
				 isGroupPresent= true;
			    	
			 }
			 
		 
		 }

		 if(!isSpecialPresent) {
			 brandsVO = new TopBrandsVO();
			 brandsVO.setFareBrand("Special");
			 brandsVO.setBookings(0);
			 brandsVO.setBookingsPercentage(0d);
			 brandsVO.setRevenue(0d);
			 brandsVO.setRevenuePercentage(0d);
			 TopBrandsList.add(brandsVO);
			 
		 }
		 if(!isSaverPresent) {
			 brandsVO = new TopBrandsVO();
			 brandsVO.setFareBrand("Saver");
			 brandsVO.setBookings(0);
			 brandsVO.setBookingsPercentage(0d);
			 brandsVO.setRevenue(0d);
			 brandsVO.setRevenuePercentage(0d);
			 TopBrandsList.add(brandsVO);
			 
		 }
		 if(!isFlexPresent) {
			 brandsVO = new TopBrandsVO();
			 brandsVO.setFareBrand("Flex");
			 brandsVO.setBookings(0);
			 brandsVO.setBookingsPercentage(0d);
			 brandsVO.setRevenue(0d);
			 brandsVO.setRevenuePercentage(0d);
			 TopBrandsList.add(brandsVO);
			 
		 }
		 if(!isFlexPlusPresent) {
			 brandsVO = new TopBrandsVO();
			 brandsVO.setFareBrand("Flex Plus");
			 brandsVO.setBookings(0);
			 brandsVO.setBookingsPercentage(0d);
			 brandsVO.setRevenue(0d);
			 brandsVO.setRevenuePercentage(0d);
			 TopBrandsList.add(brandsVO);
			 
		 }
		 if(!isGroupPresent) {
			 brandsVO = new TopBrandsVO();
			 brandsVO.setFareBrand("Group");
			 brandsVO.setBookings(0);
			 brandsVO.setBookingsPercentage(0d);
			 brandsVO.setRevenue(0d);
			 brandsVO.setRevenuePercentage(0d);
			 
			 TopBrandsList.add(brandsVO);
			 
		 }
		 
		 

		  for(int i = 0; i <TopBrandsList.size() ; i++) {
			
			if("Special".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
				
				Collections.swap(TopBrandsList, i, 0); 	
			}
			
			if("Saver".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
				
				Collections.swap(TopBrandsList, i, 1);
		    	
			}
			if("Flex".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
				
				Collections.swap(TopBrandsList, i, 2);
		    	
			}
			if("Flex Plus".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
				
				Collections.swap(TopBrandsList, i, 3);
		    	
			}
			if("Group".equalsIgnoreCase(TopBrandsList.get(i).getFareBrand())) {
				
				Collections.swap(TopBrandsList, i, 4);
		    	
			}

		  }
			
	  }


	public void initializeMonthlyBreakdownNew(PSPBookingContainer pspbc, CacheVOContainer cacheVOContainer,
				Mode thisMode, Overlay thisOverlay, List<SSRAndPaidSeatData> pspBaseBookingsForOverlay) {

		boolean dashStartUp = false;
		List<SSRAndPaidSeatData> overlayBaseBookingsForManipulation = deepCopyCombined(pspBaseBookingsForOverlay);
		List<SSRAndPaidSeatData> pspBookingsForManipulations = deepCopyCombined(pspbc.getPspBookingsNew());

		if(overlayBaseBookingsForManipulation == null){
			overlayBaseBookingsForManipulation = pspBookingsForManipulations;
			dashStartUp = true;
		}
		
		int index = 0;
		for(CacheVO cacheVO : cacheVOContainer.getCacheImages()){

			if(dashStartUp){
				updateCacheImageWithMonthlyStats(cacheVO, overlayBaseBookingsForManipulation);
			}else{
				overlayBaseBookingsForManipulation.add(pspBookingsForManipulations.get(index));
				updateCacheImageWithMonthlyStats(cacheVO, overlayBaseBookingsForManipulation);
				index++;
			}
	
		}
			
			
	}

	private void updateCacheImageWithMonthlyStats(CacheVO cacheVO,
			List<SSRAndPaidSeatData> pspBaseBookingsForOverlay) {

		ArrayList<MonthlyVO> monthlyBreakDown = new ArrayList<MonthlyVO>();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM-yy");
		Map<String, Integer> breakdownMap = new HashMap<String, Integer>();

		
		MonthlyVO newMonth = null;
		Integer totalSeatsOverTheYear = 0;
		
		for(SSRAndPaidSeatData pspBooking : pspBaseBookingsForOverlay){
				
				cal.setTime(pspBooking.getDepDate());
				breakdownMap.put(sdf.format(cal.getTime()), breakdownMap.get(sdf.format(cal.getTime()))==null?1: 
						             breakdownMap.get(sdf.format(cal.getTime())) + 1); 
				
				totalSeatsOverTheYear+=1;
						
		}
			
		/*
		for(String monthYear : breakdownMap.keySet()){
				
			    newMonth = new MonthlyVO();
				newMonth.setMonthYear(monthYear);
				newMonth.setSeats(breakdownMap.get(monthYear));
				monthlyBreakDown.add(newMonth);
				
		}
		
		*/
		
		Date currentDate = new Date();
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(currentDate);
		int currentMonth = cal1.get(Calendar.MONTH);
		int currentYear = cal1.get(Calendar.YEAR);
		int yearTemp = currentYear;
		int monthTemp = currentMonth;
		String monthForDisp = "";
		String yearForDisp ="";
		String keyStringIntoMap = "";
		
		for(int i =0; i < 12; i++) {
			
			monthTemp = currentMonth+i;
			if(monthTemp>11) {
				yearTemp++;
				monthTemp = monthTemp - 12;
				currentMonth = monthTemp-i;
			}
			
			monthForDisp = Months.values()[monthTemp].toString();
			yearForDisp = String.valueOf(yearTemp).substring(String.valueOf(yearTemp).length()-2);
			keyStringIntoMap = monthForDisp +"-" +yearForDisp;
			
			newMonth = new MonthlyVO();
			newMonth.setMonthYear(keyStringIntoMap);
			if(null!=breakdownMap.get(keyStringIntoMap))
				newMonth.setSeats(breakdownMap.get(keyStringIntoMap));
			else
				newMonth.setSeats(0);
			monthlyBreakDown.add(newMonth);
			
		}
		
//		Collections.sort(monthlyBreakDown, new Comparator<MonthlyVO>() {
//		    @Override
//		    public int compare(MonthlyVO a, MonthlyVO b) {
//		        return b.getSeats().compareTo(a.getSeats());
//		    }
//		});
//		
		
		if(monthlyBreakDown.size()>12){ //downsizing to 12 months for display
			for(int i = monthlyBreakDown.size() - 1; i>11; i--){
				
				monthlyBreakDown.remove(i);
			}
		}
		
		//setting seats percentage 
		if(totalSeatsOverTheYear!=0)
		for(MonthlyVO monthlyVO : monthlyBreakDown) {
			monthlyVO.setSeatPercentageOverYear(Double.valueOf(String.valueOf(monthlyVO.getSeats().doubleValue()/totalSeatsOverTheYear.doubleValue() * 100)));
			
		}

		cacheVO.setMonthlySplit(monthlyBreakDown);
		
//		breakdownMap.clear();
	}	
		
		
	public static enum Months{
		
		Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec
		
	}


	public void initializeSkywardsData(PSPBookingContainer pspbc, CacheVOContainer cacheVOContainer, Mode thisMode,
			Overlay thisOverlay, List<SSRAndPaidSeatData> combinedDataListForOverlay) {
		
		boolean dashStartUp = false;
		List<SSRAndPaidSeatData> overlayBaseBookingsForManipulation = deepCopyCombined(combinedDataListForOverlay);
		//List<SSRAndPaidSeatData> overlayBaseBookingsForManipulationComparison = deepCopyCombined(combinedDataListForComparison);
		List<SSRAndPaidSeatData> pspBookingsForManipulations = deepCopyCombined(pspbc.getPspBookingsNew());

		if(overlayBaseBookingsForManipulation == null){
			overlayBaseBookingsForManipulation = pspBookingsForManipulations;
			dashStartUp = true;
		}
		
		int index = 0;
		for(CacheVO cacheVO : cacheVOContainer.getCacheImages()){

			if(dashStartUp){
				updateCacheImageWithSkywardsData(cacheVO, overlayBaseBookingsForManipulation);
			}else{
				overlayBaseBookingsForManipulation.add(pspBookingsForManipulations.get(index));
				updateCacheImageWithSkywardsData(cacheVO, overlayBaseBookingsForManipulation);
				index++;
			}
	
		}
		
	}

	private void updateCacheImageWithSkywardsData(CacheVO cacheVO,
			List<SSRAndPaidSeatData> overlayBaseBookingsForManipulation) {
		
		List<SkywardMembers> skywardMembers = new ArrayList<SkywardMembers>();
		SkywardMembers skywardMember = null;
		int sumOfSeatsForAll = 0;
		Double sumOfRevenuesForAll = 0d;
		Map<String, Integer> mapOfSeatsPertype = new HashMap<String, Integer>();
		Map<String, Double> mapOfRevenuesPerType = new HashMap<String, Double>();
		int numberOfSeatsForCurrentTypeTEMP = 0;
		Double revenueForCurrentTypeTEMP = 0d;
		HashMap<String, Double> currencyMap = (HashMap<String, Double>) DashCacheUtil.getDataFromCache(DashConstants.CURRENCY_RATES_TABLE);
		Double rateToAED = 1d;
		
		for(SSRAndPaidSeatData pspBooking : overlayBaseBookingsForManipulation) {
			rateToAED = currencyMap.get(pspBooking.getCurrencyCode());
			if(rateToAED==null)rateToAED = 1d;
			
			if(null!=mapOfSeatsPertype.get(pspBooking.getSkywardsID())){
				numberOfSeatsForCurrentTypeTEMP  = mapOfSeatsPertype.get(pspBooking.getSkywardsID()).intValue() + 1;// pspBooking.getTotalPax();
			}else{
				numberOfSeatsForCurrentTypeTEMP  = 1;//pspBooking.getTotalPax();
			}
			mapOfSeatsPertype.put(pspBooking.getSkywardsID(),numberOfSeatsForCurrentTypeTEMP);

			if(null!=mapOfRevenuesPerType.get(pspBooking.getSkywardsID())){
//				revenueForCurrentTypeTEMP = mapOfRevenuesPerChannel.get(pspBooking.getChannel()).doubleValue() + (pspBooking.getAdultFare()==null?0:pspBooking.getAdultFare().doubleValue()*rateToAED) +
//	                    						(pspBooking.getOfwFare()==null?0:pspBooking.getOfwFare().doubleValue()*rateToAED) + (pspBooking.getChildFare()==null?0:pspBooking.getChildFare().doubleValue()*rateToAED) +
//	                    						(pspBooking.getTeenagerFare()==null?0:pspBooking.getTeenagerFare().doubleValue()*rateToAED);
				revenueForCurrentTypeTEMP = mapOfRevenuesPerType.get(pspBooking.getSkywardsID()).doubleValue() + 
												Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
												Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentTypeTEMP = mapOfRevenuesPerType.get(pspBooking.getSkywardsID()).doubleValue() + 
//						Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//						Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+
//						Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
				
				
				
//				revenueForCurrentTypeTEMP = mapOfRevenuesPerType.get(pspBooking.getSkywardsID()).doubleValue() +  (pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//				 (pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
				
			}else{
//				revenueForCurrentChannelTEMP = (pspBooking.getAdultFare()==null?0:pspBooking.getAdultFare().doubleValue()*rateToAED) +
//						(pspBooking.getOfwFare()==null?0:pspBooking.getOfwFare().doubleValue()*rateToAED) + (pspBooking.getChildFare()==null?0:pspBooking.getChildFare().doubleValue()*rateToAED) +
//						(pspBooking.getTeenagerFare()==null?0:pspBooking.getTeenagerFare().doubleValue()*rateToAED);
				
				revenueForCurrentTypeTEMP = 	Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
												Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
				//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//				revenueForCurrentTypeTEMP = 	Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//						Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED +
//						Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
				
//				revenueForCurrentTypeTEMP = (pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//						 (pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
				
				
			}
			mapOfRevenuesPerType.put(pspBooking.getSkywardsID(), revenueForCurrentTypeTEMP);
				
			sumOfSeatsForAll+= 1;//pspBooking.getTotalPax();
			sumOfRevenuesForAll+=Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
										 Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED;
			//UN-COMMENT BELOW STATEMENT FOR TAX AND COMMENT ABOVE
//			sumOfRevenuesForAll+=Double.valueOf(pspBooking.getBaseFare()==null||pspBooking.getBaseFare().equalsIgnoreCase("")?"0":pspBooking.getBaseFare())*rateToAED +
//					 Double.valueOf(pspBooking.getExchangedFare()==null||pspBooking.getExchangedFare().equalsIgnoreCase("")?"0":pspBooking.getExchangedFare())*rateToAED+
//					 Double.valueOf(pspBooking.getTax()==null?0:pspBooking.getTax().doubleValue()*rateToAED);
			
//			sumOfRevenuesForAll+=(pspBooking.getTotal()==null?0:pspBooking.getTotal().doubleValue()*rateToAED)+
//					 (pspBooking.getTotalTax()==null?0:pspBooking.getTotalTax().doubleValue()*rateToAED);
			
			
		}
		
		mapOfRevenuesPerType = MapSortUtil.sortByValueDescending(mapOfRevenuesPerType);
		mapOfSeatsPertype = MapSortUtil.sortByValueDescending(mapOfSeatsPertype);
		//Ordering for UI is only by seats
		List<Map.Entry<String, Integer>> listPSPSeatsByType = new LinkedList<Map.Entry<String, Integer>>(mapOfSeatsPertype.entrySet());
		
		for(int i = 0; i<listPSPSeatsByType.size(); i++){
			if(null!=listPSPSeatsByType && null!=listPSPSeatsByType.get(i)){
				skywardMember = new SkywardMembers();
				switch(listPSPSeatsByType.get(i).getKey()){
				
					case "non member" : skywardMember.setType("non member");break;
					case "member" : skywardMember.setType("member");break;
					default : skywardMember.setType("non member"); break;
				
				}
				skywardMember.setBookingsPercentage(mapOfSeatsPertype.get(listPSPSeatsByType.get(i).getKey()).doubleValue() / 
										sumOfSeatsForAll * 100);
				skywardMember.setBookings(mapOfSeatsPertype.get(listPSPSeatsByType.get(i).getKey()));
				
				skywardMember.setRevenue(mapOfRevenuesPerType.get(listPSPSeatsByType.get(i).getKey()));
				skywardMember.setRevenuePercentage(mapOfRevenuesPerType.get(listPSPSeatsByType.get(i).getKey()).doubleValue() /
						                       sumOfRevenuesForAll * 100);
				skywardMembers.add(skywardMember);
			}
			
			
		}
		
		//sortChannelsForUIRequirement(channelList);
		
		cacheVO.setSkywardMembers(skywardMembers);
		
		
		//clearing maps 
		listPSPSeatsByType = null;
		currencyMap = null;
		mapOfSeatsPertype = null;
		mapOfRevenuesPerType = null;
		
		
		
		
	}

	
}




	

