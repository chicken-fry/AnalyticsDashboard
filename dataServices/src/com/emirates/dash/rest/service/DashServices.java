package com.emirates.dash.rest.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.emirates.dash.dao.LocationXMLReader;
import com.emirates.dash.dto.MasterResponseDTO;
import com.emirates.dash.dto.PullResponseDTO;
import com.emirates.dash.utilities.DashCacheUtil;
import com.emirates.dash.utilities.Mode;
import com.emirates.dash.utilities.Overlay;
import com.emirates.dash.vo.CacheVOContainer;
import com.google.gson.Gson;
import com.sun.xml.internal.ws.util.StringUtils;


@Path("/DashServices")
public class DashServices {


	private static Gson gson;
	private static Gson getLocalGson(){
		if(gson==null){
			gson = new Gson();
		}
		return gson;
			
	}
		
	//this service is used to feed the UI every minute
	//mode describes paid seat/bookings etc
	@GET
	@Path("/pull.json")
	@Produces({"application/json"})
	public String pull(@QueryParam("mode") String mode){
			
		PullResponseDTO pullDTO = new PullResponseDTO();
		List<CacheVOContainer> cacheVOContainers = new ArrayList<CacheVOContainer>();
		CacheVOContainer cacheVOContainerTemp = null;
		
		for(Mode thisMode : Mode.values()){
			if(thisMode.toString().equalsIgnoreCase(mode)){
				for(Overlay thisOverlay : Overlay.values()){
					if(Overlay.TODAY.toString().equalsIgnoreCase(thisOverlay.toString())){
						cacheVOContainerTemp = (CacheVOContainer)DashCacheUtil.getDataFromCache(thisMode.toString()+"-"+thisOverlay.toString());
						if(cacheVOContainerTemp.isReadByUI()){
							return "";
						}
						cacheVOContainers.add(cacheVOContainerTemp);
						//cacheVOContainerTemp.setReadByUI(true);
						DashCacheUtil.setDataInCache(thisMode.toString()+"-"+thisOverlay.toString(), cacheVOContainerTemp, cacheVOContainerTemp.getLastLoadedTime());
					}
				}
			}
		}
		
		pullDTO.setCacheVOContainers(cacheVOContainers);
		return getLocalGson().toJson(pullDTO);
	
	}
	
	
	
    //Master data with locations and latitude and longitudes
	@GET
	@Path("/pullMaster.json")
	@Produces({"application/json"})
	public String pullMaster(){
			
		MasterResponseDTO mrto = LocationXMLReader.retrieveLocationsJSON();
		System.out.println("CHECKING FOR LAT LONG MAP:::" + mrto.getEKLocations().get("BNE").getLatitudeAirport());
			
		return getLocalGson().toJson(mrto);
			
		
	}
	

	/**
	 * Password control
	 * @param mode
	 * @return
	 */
	@POST
	@Path("/authorize.json")
	@Produces({"application/json"})
	public String authorize(@FormParam("plainTextPass") String plainTextPass, 
							@FormParam("plainTextUser") String plainTextUser){
			
		PullResponseDTO pullDTO = new PullResponseDTO();

		return getLocalGson().toJson(pullDTO);
	
	}
	
	
	
	
}
