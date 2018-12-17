package com.emirates.dash.dto;

import java.util.HashMap;

import com.emirates.dash.vo.LatLongVO;

public class MasterResponseDTO {

	//key = airportCode??
	private HashMap<String,LatLongVO> EKLocations;
	private HashMap<String,LatLongVO> countries;
	
	

	public HashMap<String, LatLongVO> getCountries() {
		return countries;
	}

	public void setCountries(HashMap<String, LatLongVO> countries) {
		this.countries = countries;
	}

	public HashMap<String, LatLongVO> getEKLocations() {
		return EKLocations;
	}

	public void setEKLocations(HashMap<String, LatLongVO> eKLocations) {
		EKLocations = eKLocations;
	}
	
	
	
}
